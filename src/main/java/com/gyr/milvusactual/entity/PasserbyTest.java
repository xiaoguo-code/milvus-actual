package com.gyr.milvusactual.entity;


import lombok.Data;

@Data
public class PasserbyTest {

    /**
     * 集合名称(库名)
     */
    public static String COLLECTION_NAME;
    /**
     * 集合描述
     */
    public static String COLLECTION_DESCRIPTION;
    /**
     * 分片数量
     */
    public static Integer SHARDS_NUM;
    /**
     * 分区数量
     */
    public static Integer PARTITION_NUM;

    /**
     * 分区前缀
     */
    public static String PARTITION_PREFIX;
    /**
     * 特征值长度
     */
    public static Integer FEATURE_DIM;

    /**
     * 字段
     */
    public static class Field {

        /**
         * 人脸id
         */
        public static String faceId = "face_id";

        /**
         * 质量分
         */
        public static String gridId = "gridId";

        /**
         * 抓拍时间
         */
        public static String captureTime = "captureTime";

        /**
         * 人脸特征值
         */
        public static String feature = "face_feature";
    }


}
