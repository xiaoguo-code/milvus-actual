package com.gyr.milvusactual;

import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.entity.Face;
import com.gyr.milvusactual.entity.FaceTest;
import com.gyr.milvusactual.entity.PasserbyCollectionConfig;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.partition.CreatePartitionParam;
import io.milvus.param.partition.HasPartitionParam;
import io.milvus.response.GetCollStatResponseWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @desc:
 * @Author: guoyr
 * @Date: 2022-02-14 23:01
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MilvusActualApplication.class})
public class DataInsertOperationTest {


    @Autowired
    private VectorDbService milvusService;


    @Autowired
    private MilvusServiceClient milvusServiceClient;

    final String collectionName = "passerby_202202208";


    /**
     * 模拟路人库按时间创建集合，按区域创建分区进行入库
     */
    @Test
    public void mainInsertTest() {

        //删除集合
        dropCollection(collectionName);

        //创建集合
        createCollection(collectionName, "厦门市路人库举例");

        //创建分区-可不创建，默认default分区, 分区：将收集的数据划分为物理存储上的多个部分，每个分区可以包含多个segment
        createPartition(collectionName, "SM");//思明区
        createPartition(collectionName, "HL");//湖里区
        createPartition(collectionName, "TA");//同安区
        createPartition(collectionName, "XA");//翔安区
        createPartition(collectionName, "JM");//集美区
        createPartition(collectionName, "HC");//海沧区

        //创建索引
        createIndex(collectionName);


        //插入数据
        dataInsert(collectionName, 10);

        //获取集合大小
        collectionStatistics(collectionName);

    }

    /**
     * 查看集合大小
     */
    @Test
    public void collectionStatistics() {

        //获取集合大小
        collectionStatistics(collectionName);
    }


    private void collectionStatistics(String collectionName) {
        //查看集合大小
        R<GetCollectionStatisticsResponse> respCollectionStatistics = milvusServiceClient.getCollectionStatistics(
                GetCollectionStatisticsParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        GetCollStatResponseWrapper wrapperCollectionStatistics = new GetCollStatResponseWrapper(respCollectionStatistics.getData());
        System.out.println("集合行数: " + wrapperCollectionStatistics.getRowCount());
    }

    /**
     * 创建索引
     *
     * @param collectionName
     */
    private void createIndex(String collectionName) {


        /*注：
            Milvus 支持的大多数向量索引类型都使用近似最近邻搜索（ANNS）。
            与通常非常耗时的准确检索相比，ANNS 的核心思想不再局限于返回最准确的结果，而只是搜索目标的邻居。
             IVF_FLAT  基于量化的索引    1、高速查询2、需要尽可能高的召回率

            相似度指标：在 Milvus 中，相似性度量用于衡量向量之间的相似性。选择一个好的距离度量有助于显着提高分类和聚类性能。
            浮点嵌入的选择以下两种指标：
            L2:欧几里得距离 (L2)
            IP:内积 (IP)
            。。。
         */
        //创建索引
        final String INDEX_PARAM = "{\"nlist\":1024}";     //索引构建的参数nlist（集群单元数），表示每个segment下的单元数

        R<RpcStatus> index = milvusServiceClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withFieldName(PasserbyCollectionConfig.Field.FEATURE)         //字段名
                        .withIndexType(IndexType.IVF_FLAT)        //索引类型，
                        .withMetricType(MetricType.L2)    //设置指标类型，距离的计算方式
                        .withExtraParam(INDEX_PARAM)      //外加参数
                        .withSyncMode(Boolean.FALSE)      //同步模式，默认为true
                        .withSyncWaitingInterval(500L)    //同步等待间隔，默认500毫秒
                        .withSyncWaitingTimeout(600L)      //同步等待超时，默认600秒
                        .build());

        System.out.println("创建指定集合索引，response：" + index.toString());
    }


    /**
     * 创建分区，所有分区的结构都一样的
     */
    private void createPartition(String collectionName, String partitionName) {
        //判断分区是否存在,不存在则创建
        R<Boolean> respHasPartition = milvusServiceClient.hasPartition(
                HasPartitionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withPartitionName(partitionName)
                        .build()
        );
        //判断不存在则创建
        if (Boolean.FALSE.equals(respHasPartition.getData())) {
            R<RpcStatus> respCreatePartition = milvusServiceClient.createPartition(
                    CreatePartitionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withPartitionName(partitionName)
                            .build());
            System.out.println("创建指定集合分区:" + partitionName + "，response：" + respCreatePartition.toString());
        }
    }


    private void dropCollection(String collectionName) {
        R<RpcStatus> response = milvusService.collectionManage().dropCollection(collectionName);
        System.out.println("删除集合，response：" + response.toString());
    }

    /**
     * 创建集合
     *
     * @param collectionName
     * @param description
     */
    private void createCollection(String collectionName, String description) {

        //判断集合是否存在
        HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()   //构建参数
                .withCollectionName(collectionName)    //就一个集合名称
                .build();
        R<Boolean> response = milvusServiceClient.hasCollection(hasCollectionParam);
        System.out.println("集合是否存在，response：" + response);

        if (Boolean.FALSE.equals(response.getData())) {
            //创建集合
            CreateCollectionParam createCollectionReq = getCreateCollectionParam(collectionName, description);
            R<RpcStatus> respCreateCollection = milvusServiceClient.createCollection(createCollectionReq);
            System.out.println("创建集合，response：" + respCreateCollection.toString());
        }
    }

    /**
     * 集合表结构
     *
     * @return
     */
    private CreateCollectionParam getCreateCollectionParam(String collectionName, String description) {

        //face_id
        FieldType fieldType1 = FieldType.newBuilder()
                .withName(PasserbyCollectionConfig.Field.ID)               //创建的字段名称
                .withDataType(DataType.Int64)     //创建的数据类型
                .withPrimaryKey(true)             //是否作为主键
                .withAutoID(false)                //是否自动ID（主键）分配
                .withDescription("face_id")
                .build();
        //quality_score
        FieldType fieldType2 = FieldType.newBuilder()
                .withName(PasserbyCollectionConfig.Field.QUALITY_SCORE)
                .withDataType(DataType.Double)
                .withDescription("quality_score")
                .build();
        //lon
        FieldType lon = FieldType.newBuilder()
                .withName("lon")
                .withDataType(DataType.Double)
                .withDescription("lon")
                .build();
        //lat
        FieldType lat = FieldType.newBuilder()
                .withName("lat")
                .withDataType(DataType.Double)
                .withDescription("lat")
                .build();
        //feature
        FieldType fieldType3 = FieldType.newBuilder()
                .withName(PasserbyCollectionConfig.Field.FEATURE)
                .withDataType(DataType.FloatVector)  //浮点向量字段
                .withDimension(PasserbyCollectionConfig.FEATURE_DIM)
                .withDescription("feature")//向量维度，这里表示一个名为feature的二维浮点向量字段
                .build();

        //集合对象
        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)             //集合名称
                .withDescription(PasserbyCollectionConfig.COLLECTION_DESCRIPTION)           //集合描述
                .withShardsNum(PasserbyCollectionConfig.SHARDS_NUM)                      //分片数量，这里表示双分片
                .addFieldType(fieldType1)              //添加字段
                .addFieldType(fieldType2)
                .addFieldType(lon)
                .addFieldType(lat)
                .addFieldType(fieldType3)               //或者withFieldTypes(fieldTypes)

                .build();

        return createCollectionReq;
    }

    /**
     * 数据插入
     */
    private R<MutationResult> dataInsert(String collectionName, Integer num) {
        Double lon = 12.123456d;

        //模拟总数20000条的数据
        List<String> areaList = Arrays.asList("SM", "HL", "TA", "XA", "JM", "HC");
        List<Face> faceList = new ArrayList<>();
        for (int i = 0; i < num; ++i) {
            Face face = new Face();
            face.setFaceId((long) i);
            face.setQualityScore(Math.random());
            face.setLon(lon);
            face.setLat(lon);
            face.setArea(areaList.get(new Random().nextInt(6)));
            faceList.add(face);
        }

        Map<String, List<Face>> listMap = faceList.stream().collect(Collectors.groupingBy(Face::getArea));

        listMap.forEach((area, faces) -> {
            Random ran = new Random();

            //创建集合是的face_id
            List<Long> face_id_array = new ArrayList<>();
            //quality_score
            List<Double> quality_score_array = new ArrayList<>();

            List<Double> lon_array = new ArrayList<>();
            List<Double> lat_array = new ArrayList<>();

            //feature
            List<List<Float>> feature_array = new ArrayList<>();
            for (Face face : faces) {

                face_id_array.add(face.getFaceId());

                quality_score_array.add(face.getQualityScore());
                lon_array.add(face.getLon());

                lat_array.add(face.getLat());

                //向量
                List<Float> vector = new ArrayList<>();
                for (int k = 0; k < PasserbyCollectionConfig.FEATURE_DIM; ++k) {
                    vector.add(ran.nextFloat());
                }
                feature_array.add(vector);
            }
            List<InsertParam.Field> fields = new ArrayList<>();
//            fields.add(new InsertParam.Field(PasserbyCollectionConfig.Field.ID, DataType.Int64, face_id_array));
//            fields.add(new InsertParam.Field(PasserbyCollectionConfig.Field.QUALITY_SCORE, DataType.Double, quality_score_array));
//            fields.add(new InsertParam.Field("lon", DataType.Double, lon_array));
//            fields.add(new InsertParam.Field("lat", DataType.Double, lat_array));
//            fields.add(new InsertParam.Field(PasserbyCollectionConfig.Field.FEATURE, DataType.FloatVector, feature_array));
            //入库对象构建
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withPartitionName(area)
                    .withFields(fields)
                    .build();
            long startTime = System.currentTimeMillis();
            R<MutationResult> respInsert = milvusServiceClient.insert(insertParam);
            long endTime = System.currentTimeMillis();
            System.out.println("插入数据至" + collectionName + "的" + area +
                    "分区,耗时：" + (endTime - startTime) + ",response：" + respInsert.toString());
        });


        return null;
    }



    /**
     * 数据插入
     */
    private R<MutationResult> dataInsert(String collectionName, String partitionName, List<FaceTest> faces) {

        //创建集合是的face_id
        List<Long> face_id_array = new ArrayList<>();
        List<Long> grid_id_array = new ArrayList<>();
        List<Long> capture_time_array = new ArrayList<>();
        //feature
        List<List<Float>> feature_array = new ArrayList<>();

        Random ran = new Random();
        for (FaceTest face : faces) {
            face_id_array.add(face.getFaceId());
            grid_id_array.add(face.getGridId());
            capture_time_array.add(face.getCaptureTime());
            //向量
            List<Float> vector = new ArrayList<>();
            for (int k = 0; k <512; ++k) {
                vector.add(ran.nextFloat());
            }
            feature_array.add(vector);
        }




        List<InsertParam.Field> fields = new ArrayList<>();
//        fields.add(new InsertParam.Field("face_id", DataType.Int64, face_id_array));
//        fields.add(new InsertParam.Field("grid_id", DataType.Int64, grid_id_array));
//        fields.add(new InsertParam.Field("capture_time", DataType.Int64, capture_time_array));
//        fields.add(new InsertParam.Field("face_feature", DataType.FloatVector, feature_array));
        //入库对象构建
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withPartitionName(partitionName)
                .withFields(fields)
                .build();
        R<MutationResult> respInsert = milvusServiceClient.insert(insertParam);
        return respInsert;
    }


    @Test
    public void test111(){
        List<FaceTest> faces  = new ArrayList<>();
        for (long i = 0; i < 2000; ++i) {
            FaceTest face = new FaceTest();
            face.setFaceId(i);
            face.setGridId(i);
            face.setCaptureTime(i);
//            face.setArea(areaList.get(new Random().nextInt(6)));
            faces.add(face);
        }

        dataInsert("passerby_20220219","face",faces);
    }


}
