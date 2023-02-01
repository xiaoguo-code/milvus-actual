package com.gyr.milvusactual.dao;

import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.GetCollStatResponseWrapper;

/**
 * 集合管理
 */
public interface CollectionManageService {


    /*--------------------集合管理--------------------*/
    /**
     * 集合是否存在
     *
     * @param collectionName
     * @return
     */
    R<Boolean> hasCollection(String collectionName);

    /**
     * 获取集合列表
     * @return
     */
    R<ShowCollectionsResponse> showCollections();

    /**
     * 获取集合详情
     * @param collectionName
     * @return
     */
    DescCollResponseWrapper describeCollection(String collectionName);

    /**
     * 获取集合大小
     * @param collectionName
     * @return
     */
    GetCollStatResponseWrapper getCollectionStatistics(String collectionName);

    /**
     * 创建集合
     */
    R<RpcStatus> createCollection(CreateCollectionParam createCollectionReq);

    /**
     * 删除集合
     * @param collectionName
     * @return
     */
    R<RpcStatus> dropCollection(String collectionName);


    /*--------------------集合别名管理--------------------*/
    /**
     * 创建集合别名
     * @param collectionName
     * @param aliasName
     */
    R<RpcStatus> createAlias(String collectionName,String aliasName);

    /**
     * 修改集合别名
     * @param collectionName
     * @param aliasName
     * @return
     */
    R<RpcStatus> alterAlias(String collectionName,String aliasName);

    /**
     * 删除集合别名
     * @param collectionName
     * @param aliasName
     * @return
     */
    R<RpcStatus> dropAlias(String collectionName,String aliasName);


    /*--------------------加载集合--------------------*/
    /*milvus当前使用版本2.0.0，要加载的数据量必须低于所有查询节点总内存资源的 90%，以便为执行引擎预留内存资源。*/
    /**
     * 加载集合到内存里
     * @param collectionName
     * @return
     */
    R<RpcStatus> loadCollection(String collectionName);

    /**
     * 释放集合--从内存中释放集合以减少内存使用
     * @param collectionName
     * @return
     */
    R<RpcStatus> releaseCollection(String collectionName);
}
