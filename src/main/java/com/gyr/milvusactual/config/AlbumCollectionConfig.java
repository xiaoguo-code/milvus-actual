package com.gyr.milvusactual.config;


import io.milvus.grpc.DataType;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;

import java.util.ArrayList;
import java.util.List;

/**
 * 底库集合详情配置
 */
public class AlbumCollectionConfig {

    /**
     * 集合默认名称(库名)
     */
    public static final String COLLECTION_NAME = "people";
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
    public static final Integer PARTITION_NUM = 1;

    /**
     * 分区前缀
     */
    public static final String PARTITION_PREFIX = "shards_";
    /**
     * 特征值维度，[经度，维度，性别，年龄，身高，体重]
     */
    public static final Integer FEATURE_DIM = 256;

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
         * 质量分
         */
        public static final String QUALITY_SCORE = "quality_score";
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
     * 获取所有分区
     * @return
     */
    public static List<String> getAllPertitionName() {
        List<String> partitions = new ArrayList<>();
        for (Integer i = 1; i <= PARTITION_NUM; i++) {
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
                .withName(AlbumCollectionConfig.Field.ID)               //创建的字段名称
                .withDataType(DataType.Int64)     //创建的数据类型
                .withPrimaryKey(true)             //是否作为主键
                .withAutoID(false)                //是否自动ID（主键）分配
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
                .withDataType(DataType.FloatVector)  //浮点向量字段
                .withDimension(AlbumCollectionConfig.FEATURE_DIM)
                .withDescription("feature")//向量维度，这里表示一个名为feature的二维浮点向量字段
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
}
