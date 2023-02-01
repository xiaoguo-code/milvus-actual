package com.gyr.milvusactual.dao.impl;


import com.gyr.milvusactual.dao.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MilvusServiceImpl implements MilvusService{


    @Autowired
    private CollectionManageService collectionManageService;

    @Autowired
    private DataManageService dataManageService;

    @Autowired
    private PartitionManageService partitionManageService;

    @Autowired
    private IndexsManageService indexsManageService;

    /**
     * 集合管理相关操作
     *
     * @return
     */
    @Override
    public CollectionManageService CollectionManage() {
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
