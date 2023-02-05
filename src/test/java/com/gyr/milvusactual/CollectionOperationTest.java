package com.gyr.milvusactual;

import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.entity.FaceTest;
import com.gyr.milvusactual.config.AlbumCollectionConfig;
import com.gyr.milvusactual.entity.People;
import com.gyr.milvusactual.service.BusinessOperationService;
import com.gyr.milvusactual.util.DataMockUtil;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @desc: 集合操作
 * @Author: guoyr
 * @Date: 2022-02-14 23:01
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MilvusActualApplication.class})
public class CollectionOperationTest {

    @Autowired
    private BusinessOperationService businessOperationService;

    @Autowired
    private VectorDbService vectorDbService;


    @Autowired
    private GenericObjectPool<MilvusServiceClient> milvusServiceClientGenericObjectPool;

    @Autowired
    private DataMockUtil dataMockUtil;


    /**
     * 删除集合
     */
    @Test
    public void dropCollectionTest() {
        businessOperationService.dropCollection(AlbumCollectionConfig.COLLECTION_NAME);
    }

    /**
     * 创建集合
     */
    @Test
    public void createCollectionTest() {

        //删除集合
        dropCollectionTest();

        //创建集合
        businessOperationService.createCollection(AlbumCollectionConfig.COLLECTION_NAME, "测试库");

        //创建分区-可不创建，默认default分区, 分区：将收集的数据划分为物理存储上的多个部分，每个分区可以包含多个segment
        createPartitionTest();

        //创建索引
        createIndexTest();


        //插入数据
        dataInsertCollectionTest();

        //获取集合大小
        collectionStatisticsTest();

    }

    /**
     * 创建分区
     */
    @Test
    public void createPartitionTest() {
        for (Integer i = 0; i < AlbumCollectionConfig.PARTITION_NUM; i++) {
            businessOperationService.createPartition(AlbumCollectionConfig.COLLECTION_NAME, AlbumCollectionConfig.getPartitionName(i));//思明区
        }
//        businessOperationService.createPartition(PasserbyCollectionConfig.COLLECTION_NAME, "SM");//思明区
//        businessOperationService.createPartition(PasserbyCollectionConfig.COLLECTION_NAME, "HL");//湖里区
//        businessOperationService.createPartition(PasserbyCollectionConfig.COLLECTION_NAME, "TA");//同安区
//        businessOperationService.createPartition(PasserbyCollectionConfig.COLLECTION_NAME, "XA");//翔安区
//        businessOperationService.createPartition(PasserbyCollectionConfig.COLLECTION_NAME, "JM");//集美区
//        businessOperationService.createPartition(PasserbyCollectionConfig.COLLECTION_NAME, "HC");//海沧区
    }

    /**
     * 创建索引
     */
    @Test
    public void createIndexTest() {
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
        MilvusServiceClient milvusServiceClient = null;
        try {
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> index = milvusServiceClient.createIndex(
                    CreateIndexParam.newBuilder()
                            .withCollectionName(AlbumCollectionConfig.COLLECTION_NAME)
                            .withFieldName(AlbumCollectionConfig.Field.FEATURE)         //字段名
                            .withIndexType(IndexType.IVF_FLAT)        //索引类型，
                            .withMetricType(MetricType.L2)    //设置指标类型，距离的计算方式
                            .withExtraParam(INDEX_PARAM)      //外加参数
                            .withSyncMode(Boolean.FALSE)      //同步模式，默认为true
//                        .withSyncWaitingInterval(500L)    //同步等待间隔，默认500毫秒
//                        .withSyncWaitingTimeout(600L)      //同步等待超时，默认600秒
                            .build());

            System.out.println("创建指定集合索引，response：" + index.toString());
            R<DescribeIndexResponse> describeIndexResp = milvusServiceClient.describeIndex(
                    DescribeIndexParam.newBuilder()
                            .withCollectionName(AlbumCollectionConfig.COLLECTION_NAME)
                            .build());
            System.out.println("describeIndex查看执行集合索引,response:" + describeIndexResp.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
    }


    /**
     * 数据插入
     */
    @Test
    public void dataInsertCollectionTest() {

        //模拟总数10000条的数据
        Integer num = 10000;
        List<People> peoples = dataMockUtil.mockPeople(num);

        Map<String, List<People>> map = peoples.stream().collect(Collectors.groupingBy(People::getPartition));
        map.forEach((partition, peopleList) -> {
            //创建集合是的face_id
            List<Long> id_array = new ArrayList<>();
            List<String> name_array = new ArrayList<>();
            //feature
            List<List<Float>> feature_array = new ArrayList<>();

            for (People people : peopleList) {
                id_array.add(people.getId());
                name_array.add(people.getName());
                feature_array.add(people.getFeature());
            }

            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field(AlbumCollectionConfig.Field.ID, id_array));
            fields.add(new InsertParam.Field(AlbumCollectionConfig.Field.NAME, name_array));
            fields.add(new InsertParam.Field(AlbumCollectionConfig.Field.FEATURE, feature_array));

            //入库对象构建
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(AlbumCollectionConfig.COLLECTION_NAME)
                    .withPartitionName(partition)  //未指定默认入_default
                    .withFields(fields)
                    .build();
            long startTime = System.currentTimeMillis();
            MilvusServiceClient milvusServiceClient = null;
            try {
                milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
                R<MutationResult> respInsert = milvusServiceClient.insert(insertParam);
                long endTime = System.currentTimeMillis();
                System.out.println("插入数据至" + AlbumCollectionConfig.COLLECTION_NAME + "-" + partition + ",耗时：" + (endTime - startTime) + ",response：" + respInsert.toString());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 回收对象到对象池
                if (milvusServiceClient != null) {
                    milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
                }
            }
        });
    }


    /**
     * 查看集合大小
     */
    @Test
    public void collectionStatisticsTest() {
        //查看集合大小
        long collectionStatistics = vectorDbService.getCollectionStatistics(AlbumCollectionConfig.COLLECTION_NAME);
        System.out.println("集合行数: " + collectionStatistics);
    }


}
