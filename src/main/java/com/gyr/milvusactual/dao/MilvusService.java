package com.gyr.milvusactual.dao;


import io.milvus.param.R;

/**
 * milvus
 */
public interface MilvusService {


    /**
     * 集合管理相关操作
     * @return
     */
    CollectionManageService CollectionManage();


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
