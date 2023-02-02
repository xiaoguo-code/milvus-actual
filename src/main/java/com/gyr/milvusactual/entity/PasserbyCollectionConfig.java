package com.gyr.milvusactual.entity;


public class PasserbyCollectionConfig {

    /**
     * 集合名称(库名)
     */
    public static final String COLLECTION_NAME = "people";
    /**
     * 集合描述
     */
    public static final String COLLECTION_DESCRIPTION = "人员信息库";
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
     * 特征值维度，[经度，维度，性别，年龄，身高，体重]
     */
    public static final Integer FEATURE_DIM = 6;

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

}
