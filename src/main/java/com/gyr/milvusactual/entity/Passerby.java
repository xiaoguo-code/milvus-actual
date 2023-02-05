package com.gyr.milvusactual.entity;



public  class Passerby {

    /**
     * 集合名称(库名)
     */
    public static final String COLLECTION_NAME = "passerby";
    /**
     * 集合描述
     */
    public static final String COLLECTION_DESCRIPTION = "passerby";
    /**
     * 分片数量
     */
    public static final Integer SHARDS_NUM = 4;
    /**
     * 分区数量
     */
    public static final Integer PARTITION_NUM = 8;

    /**
     * 分区前缀
     */
    public static final String PARTITION_PREFIX = "shards_";
    /**
     * 特征值长度
     */
    public static final Integer FEATURE_DIM = 512;

    /**
     * 字段
     */
    public static class Field {
        /**
         * 人脸id
         */
        public static final String FACE_ID = "face_id";
        /**
         * 质量分
         */
        public static final String QUALITY_SCORE = "quality_score";
        /**
         * 人脸特征值
         */
        public static final String FACE_FEATURE = "face_feature";
    }

    /**
     * 通过人脸id计算分区名称
     * @param captureTime
     * @return
     */
    public static String getPartitionName(Integer captureTime) {
        return PARTITION_PREFIX + "2022021"+captureTime;
    }

}
