package com.gyr.milvusactual.dao;


import io.milvus.param.R;

/**
 * 向量数据库服务接口
 */
public interface VectorDbService {


    /**
     * 集合管理相关操作
     * @return
     */
    CollectionManageService collectionManage();


    /**
     * 数据管理相关操作
     * @return
     */
    DataManageService dataManage();


    /**
     * 集合分区管理相关操作
     * @return
     */
    PartitionManageService partitionManage();

    /**
     * 索引管理相关操作
     * @return
     */
    IndexsManageService indexsManage();

}
