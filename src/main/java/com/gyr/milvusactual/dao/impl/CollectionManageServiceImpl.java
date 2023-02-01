package com.gyr.milvusactual.dao.impl;


import com.gyr.milvusactual.dao.CollectionManageService;
import com.gyr.milvusactual.entity.Passerby;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.alias.AlterAliasParam;
import io.milvus.param.alias.CreateAliasParam;
import io.milvus.param.alias.DropAliasParam;
import io.milvus.param.collection.*;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.GetCollStatResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.spec.PSSParameterSpec;

@Service
public class CollectionManageServiceImpl implements CollectionManageService {


    /**
     * 注入milvus连接客户端
     */
    @Autowired
    private MilvusServiceClient milvusServiceClient;


    @Override
    public R<Boolean> hasCollection(String collectionName) {
        R<Boolean> response = milvusServiceClient.hasCollection(
                HasCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        return response;
    }

    /**
     * 获取集合列表
     *
     * @return
     */
    @Override
    public R<ShowCollectionsResponse> showCollections() {
        return null;
    }

    /**
     * 获取集合详情
     *
     * @param collectionName
     * @return
     */
    @Override
    public DescCollResponseWrapper describeCollection(String collectionName) {
        R<DescribeCollectionResponse> respDescribeCollection = milvusServiceClient.describeCollection(          //返回集合名称和分配信息
                DescribeCollectionParam.newBuilder()
                        .withCollectionName("book")
                        .build());
        DescCollResponseWrapper wrapperDescribeCollection = new DescCollResponseWrapper(respDescribeCollection.getData());
        return wrapperDescribeCollection;
    }

    /**
     * 获取集合大小
     *
     * @param collectionName
     * @return
     */
    @Override
    public GetCollStatResponseWrapper getCollectionStatistics(String collectionName) {
        R<GetCollectionStatisticsResponse> respCollectionStatistics = milvusServiceClient.getCollectionStatistics(   // Return the statistics information of the collectionName.
                GetCollectionStatisticsParam.newBuilder()
                        .withCollectionName(Passerby.COLLECTION_NAME)
                        .build());
        GetCollStatResponseWrapper wrapperCollectionStatistics = new GetCollStatResponseWrapper(respCollectionStatistics.getData());
        System.out.println("Collection row count: " + wrapperCollectionStatistics.getRowCount());
        return wrapperCollectionStatistics;
    }


    /**
     * 创建集合
     * @return
     */
    @Override
    public R<RpcStatus> createCollection(CreateCollectionParam createCollectionReq) {
        //创建集合
        R<RpcStatus> response = milvusServiceClient.createCollection(createCollectionReq);
        return response;
    }

    /**
     * 删除集合--删除集合会不可逆地删除其中的所有数据。
     * @param collectionName
     * @return
     */
    @Override
    public R<RpcStatus> dropCollection(String collectionName) {
        R<RpcStatus> response = milvusServiceClient.dropCollection(
                DropCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        return response;
    }

    /**
     * 创建集合别名--集合别名是全局唯一的，因此您不能将相同的别名分配给不同的集合。但是，您可以为一个集合分配多个别名。
     * @param collectionName
     * @param aliasName
     */
    @Override
    public R<RpcStatus> createAlias(String collectionName, String aliasName) {
        R<RpcStatus> response = milvusServiceClient.createAlias(
                CreateAliasParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withAlias(aliasName)
                        .build());
        return response;
    }

    /**
     * 修改集合别名
     *
     * @param collectionName
     * @param aliasName
     * @return
     */
    @Override
    public R<RpcStatus> alterAlias(String collectionName, String aliasName) {
        R<RpcStatus> response = milvusServiceClient.alterAlias(
                AlterAliasParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withAlias(aliasName)
                        .build());
        return response;
    }

    /**
     * 删除集合别名
     *
     * @param collectionName
     * @param aliasName
     * @return
     */
    @Override
    public R<RpcStatus> dropAlias(String collectionName, String aliasName) {
        R<RpcStatus> response = milvusServiceClient.dropAlias(
                DropAliasParam.newBuilder()
                        .withAlias(collectionName)
                        .build());

        return response;
    }

    /**
     * 加载集合到内存里
     *
     * @param collectionName
     * @return
     */
    @Override
    public R<RpcStatus> loadCollection(String collectionName) {
        R<RpcStatus> response = milvusServiceClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        return response;
    }


    /**
     * 释放集合--从内存中释放集合以减少内存使用
     * @param collectionName
     * @return
     */
    @Override
    public R<RpcStatus> releaseCollection(String collectionName) {
        R<RpcStatus> response = milvusServiceClient.releaseCollection(
                ReleaseCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());

        return response;
    }
}
