package com.gyr.milvusactual;

import cn.hutool.json.JSONUtil;
import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.entity.Passerby;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.GetCollStatResponseWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MilvusActualApplication.class})
public class MilvusActualApplicationTests {

    @Autowired
    private MilvusServiceClient milvusServiceClient;

    @Autowired
    private VectorDbService milvusService;

    /**
     * 路人库
     */
    final String collectionName = "passerby";

    /**
     * 分区名-相当于分类
     */
    final String partitionName = "face";
    final String partitionName2 = "body";


    /**
     * 集合是否存在
     */
    @Test
    public void hasCollection() {

        R<Boolean> response = milvusServiceClient.hasCollection(
                HasCollectionParam.newBuilder()
                        .withCollectionName("passerby")
                        .build());

        System.out.println("集合是否存在，response：" + response);
    }

    /**
     * 创建集合
     */
    @Test
    public void createCollection() {

//        R<RpcStatus> response = milvusService.CollectionManage().createCollection();
//        System.out.println("创建集合，response：" + response.toString());


        R<RpcStatus> partition = milvusService.partitionManage().createPartition("book", "novel");
        System.out.println("创建指定集合分区，response：" + partition.toString());


        final IndexType INDEX_TYPE = IndexType.IVF_FLAT;   // IndexType
        final String INDEX_PARAM = "{\"nlist\":1024}";     // ExtraParam


        R<RpcStatus> index = milvusServiceClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName("book")
                        .withFieldName("book_intro")
                        .withIndexType(INDEX_TYPE)
                        .withMetricType(MetricType.L2)
                        .withExtraParam(INDEX_PARAM)
                        .withSyncMode(Boolean.FALSE)
                        .build());

        System.out.println("创建指定集合索引，response：" + JSONUtil.toJsonStr(index));

    }

    /**
     * 建立索引
     */
    @Test
    public void createIndex() {

        final IndexType INDEX_TYPE = IndexType.IVF_FLAT;   // IndexType
        final String INDEX_PARAM = "{\"nlist\":1024}";     // ExtraParam


        R<RpcStatus> index = milvusServiceClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName("book")
                        .withFieldName("book_intro")
                        .withIndexType(INDEX_TYPE)
                        .withMetricType(MetricType.L2)
                        .withExtraParam(INDEX_PARAM)
                        .withSyncMode(Boolean.FALSE)
                        .build());
        System.out.println("建立索引，response：" + JSONUtil.toJsonStr(index));

    }


    /**
     * 加载集合到内存中集合
     */
    @Test
    public void loadCollection() {

        R<RpcStatus> response = milvusService.collectionManage().loadCollection(collectionName);
        System.out.println("加载集合到内存，response：" + response.toString());


    }

    /**
     * 删除集合
     */
    @Test
    public void dropCollection() {

        R<RpcStatus> response = milvusService.collectionManage().dropCollection(collectionName);
        System.out.println("删除集合，response：" + response.toString());


    }

    @Test
    public void dropPartition() {

        R<RpcStatus> response = milvusService.partitionManage().dropPartition(Passerby.COLLECTION_NAME, Passerby.getPartitionName(1));
        System.out.println("删除分区，response：" + response.toString());


    }


    /**
     * 数据插入
     */
    @Test
    public void dataInsert() {

        Random ran = new Random();
        //创建集合是的book_id
        List<Long> book_id_array = new ArrayList<>();
        //word_count
        List<Long> word_count_array = new ArrayList<>();
        //book_intro
        List<List<Float>> book_intro_array = new ArrayList<>();

        //插入两千条数据
        for (long i = 0L; i < 2000; ++i) {
            book_id_array.add(i);
            word_count_array.add(i + 10000);
            List<Float> vector = new ArrayList<>();
            for (int k = 0; k < 2; ++k) {
                vector.add(ran.nextFloat());
            }
            book_intro_array.add(vector);
        }

        List<InsertParam.Field> fields = new ArrayList<>();
//        fields.add(new InsertParam.Field("book_id", DataType.Int64, book_id_array));
//        fields.add(new InsertParam.Field("word_count", DataType.Int64, word_count_array));
//        fields.add(new InsertParam.Field("book_intro", DataType.FloatVector, book_intro_array));

        //入库对象构建
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("book")
                .withPartitionName("novel")
                .withFields(fields)
                .build();
        R<MutationResult> response = milvusServiceClient.insert(insertParam);

        System.out.println("插入数据，response：" + response.toString());


    }


    /**
     * 集合大小
     */
    @Test
    public void statisticsCollection() {

        GetCollStatResponseWrapper collectionStatistics = milvusService.collectionManage().getCollectionStatistics(Passerby.COLLECTION_NAME);

        System.out.println("集合大小，response：" + collectionStatistics.getRowCount());


    }

    /**
     * 删除数据
     */
    @Test
    public void dataDrop() {

        String DELETE_EXPR = "book_id in [0,1]";


        R<MutationResult> response = milvusService.dataManage().delete("book", DELETE_EXPR);

        System.out.println("删除数据，response：" + JSONUtil.toJsonStr(response));


    }

    /**
     * 检索
     */
    @Test
    public void dataQuery() {

        final Integer SEARCH_K = 10;                       // TopK
        final String SEARCH_PARAM = "{\"nprobe\":10,\"round_decimal\":-1}";    // Params

        List<String> search_output_fields = Arrays.asList("book_id", "word_count");
        List<List<Float>> search_vectors = Arrays.asList(Arrays.asList(0.1f, 0.2f));

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName("book")
                .withPartitionNames(Arrays.asList("novel"))
                .withOutFields(search_output_fields)
                .withTopK(SEARCH_K)
                .withMetricType(MetricType.L2)
                .withVectors(search_vectors)
                .withVectorFieldName("book_intro")
//                .withExpr(exr)
                .withParams(SEARCH_PARAM)
                .build();
        R<SearchResults> respSearch = milvusServiceClient.search(searchParam);

        SearchResults data = respSearch.getData();
        SearchResultData results = data.getResults();
        //获取各个属性对象
        List<FieldData> fieldsDataList = results.getFieldsDataList();
//        results.get
        for (FieldData fieldData : fieldsDataList) {
            //字段名
            String fieldName = fieldData.getFieldName();
            //字段值列表
            ScalarField scalars = fieldData.getScalars();
            List<Long> dataList = scalars.getLongData().getDataList();
            System.out.println(fieldName + ":" + JSONUtil.toJsonStr(dataList));
        }


    }


}

























