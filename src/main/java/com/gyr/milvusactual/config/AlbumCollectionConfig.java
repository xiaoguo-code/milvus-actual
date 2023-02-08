package com.gyr.milvusactual.config;


import io.milvus.grpc.DataType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.index.CreateIndexParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 底库集合详情配置
 */
public class AlbumCollectionConfig {

    public static final String IMG_PATH = "C:/Users/41071/Pictures/img";

    /**
     * 集合默认名称(库名)
     */
    public static final String COLLECTION_NAME = "people_xa";
    /**
     * 集合描述
     */
    public static final String COLLECTION_DESCRIPTION = "人员底库";
    /**
     * 分片数量
     */
    public static final Integer SHARDS_NUM = 2;
    /**
     * 分区数量
     */
    public static final Integer PARTITION_NUM = 5;

    /**
     * 分区前缀
     */
    public static final String PARTITION_PREFIX = "shards_";
    /**
     * 特征值维度
     */
    public static final Integer FEATURE_DIM = 258;

    /**
     * 返回的topN
     */
    public static final Integer SEARCH_K = 10;

    public static final String SEARCH_PARAM = "{\"nprobe\":10}";

    /**
     * 字段
     */
    public static class Field {

        /**
         * id
         */
        public static final String ID = "id";
        /**
         * 数据id
         */
        public static final String NAME = "name";
        /**
         * 特征值
         */
        public static final String FEATURE = "feature";
    }

    /**
     * 通过人脸id计算分区名称
     *
     * @param num
     * @return
     */
    public static String getPartitionName(Integer num) {
        return PARTITION_PREFIX + (num % PARTITION_NUM + 1);
    }

    /**
     * 随机选择一个分区
     * @return 分区名
     */
    public static String getRandomPartitionName() {
        List<String> allPartitionName = getAllPartitionName();
        Random random = new Random();
        return allPartitionName.get(random.nextInt(allPartitionName.size()));
    }


    /**
     * 获取所有分区
     *
     * @return
     */
    public static List<String> getAllPartitionName() {
        List<String> partitions = new ArrayList<>();
        for (int i = 1; i <= PARTITION_NUM; i++) {
            partitions.add(PARTITION_PREFIX + i);
        }
        return partitions;
    }

    /**
     * 集合表结构
     *
     * @return
     */
    public static CreateCollectionParam getCollectionTemplate(String collectionName) {

        //id
        FieldType fieldType1 = FieldType.newBuilder()
                //创建的字段名称
                .withName(AlbumCollectionConfig.Field.ID)
                //创建的数据类型
                .withDataType(DataType.Int64)
                //是否作为主键
                .withPrimaryKey(true)
                //是否自动ID（主键）分配
                .withAutoID(false)
                .withDescription("id")
                .build();

        //name
        FieldType fieldType2 = FieldType.newBuilder()
                .withName(AlbumCollectionConfig.Field.NAME)
                .withDataType(DataType.VarChar)
                .withMaxLength(21)
                .withDescription("name")
                .build();
        //feature
        FieldType fieldType3 = FieldType.newBuilder()
                .withName(AlbumCollectionConfig.Field.FEATURE)
                //浮点向量字段
                .withDataType(DataType.FloatVector)
                .withDimension(AlbumCollectionConfig.FEATURE_DIM)
                //向量维度，这里表示一个名为feature的二维浮点向量字段
                .withDescription("feature")
                .build();

        //集合对象
        return CreateCollectionParam.newBuilder()
                //集合名称
                .withCollectionName(collectionName)
                //集合描述
                .withDescription(AlbumCollectionConfig.COLLECTION_DESCRIPTION)
                //分片数量
                .withShardsNum(AlbumCollectionConfig.SHARDS_NUM)
                //添加字段或者withFieldTypes(fieldTypes)
                .addFieldType(fieldType1)
                .addFieldType(fieldType2)
                .addFieldType(fieldType3)
                .build();
    }

    /**
     * 获取索引创建参数对象
     * @return
     */
    public static CreateIndexParam getCreateIndexParam() {
        //索引构建的参数nlist（集群单元数），表示每个segment下的单元数
        final String indexParam = "{\"nlist\":1024}";
        return CreateIndexParam.newBuilder()
                .withCollectionName(AlbumCollectionConfig.COLLECTION_NAME)
                .withFieldName(AlbumCollectionConfig.Field.FEATURE)
                //索引类型，
                .withIndexType(IndexType.IVF_FLAT)
                //设置指标类型，距离的计算方式
                .withMetricType(MetricType.L2)
                //外加参数
                .withExtraParam(indexParam)
                //同步模式，默认为true
                .withSyncMode(Boolean.FALSE)
                //同步等待间隔，默认500毫秒
//                        .withSyncWaitingInterval(500L)
                //同步等待超时，默认600秒
//                        .withSyncWaitingTimeout(600L)
                .build();

    }
}
