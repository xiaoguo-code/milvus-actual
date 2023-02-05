package com.gyr.milvusactual.dao.impl;


import com.gyr.milvusactual.dao.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MilvusServiceImpl implements VectorDbService {


    /**
     * 集合管理
     */
    @Autowired
    private CollectionManageService collectionManageService;
    /**
     * 数据管理
     */
    @Autowired
    private DataManageService dataManageService;
    /**
     * 分区管理
     */
    @Autowired
    private PartitionManageService partitionManageService;
    /**
     * 索引管理
     */
    @Autowired
    private IndexsManageService indexsManageService;


    /**
     * 集合管理相关操作
     *
     * @return
     */
    @Override
    public CollectionManageService collectionManage() {
        return collectionManageService;
    }

    /**
     * 数据管理相关操作
     *
     * @return
     */
    @Override
    public DataManageService dataManage() {
        return dataManageService;
    }

    /**
     * 集合分区管理相关操作
     *
     * @return
     */
    @Override
    public PartitionManageService partitionManage() {
        return partitionManageService;
    }

    /**
     * 索引管理相关操作
     *
     * @return
     */
    @Override
    public IndexsManageService indexsManage() {
        return indexsManageService;
    }
}
