package com.gyr.milvusactual.dao.impl;


import com.gyr.milvusactual.dao.PartitionManageService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.partition.CreatePartitionParam;
import io.milvus.param.partition.DropPartitionParam;
import io.milvus.param.partition.HasPartitionParam;
import io.milvus.param.partition.ShowPartitionsParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartitionManageServiceImpl implements PartitionManageService {


    /**
     * 注入milvus连接客户端
     */
    @Autowired
    private MilvusServiceClient milvusServiceClient;


    /**
     * 创建分区
     *
     * @param collectionName
     * @param partitionName
     * @return
     */
    @Override
    public R<RpcStatus> createPartition(String collectionName, String partitionName) {
        R<RpcStatus> response = milvusServiceClient.createPartition(
                CreatePartitionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withPartitionName(partitionName)
                        .build());
        return response;
    }

    /**
     * 验证分区是否存在
     *
     * @param collectionName
     * @param partitionName
     * @return
     */
    @Override
    public R<Boolean> hasPartition(String collectionName, String partitionName) {
        R<Boolean> response = milvusServiceClient.hasPartition(
                HasPartitionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withPartitionName(partitionName)
                        .build());
        return response;
    }

    /**
     * 获取集合的所有分区
     *
     * @param collectionName
     * @return
     */
    @Override
    public R<ShowPartitionsResponse> showPartitions(String collectionName) {
        R<ShowPartitionsResponse> response = milvusServiceClient.showPartitions(
                ShowPartitionsParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        return response;
    }

    /**
     * 删除指定集合中的分区
     *
     * @param collectionName
     * @param partitionName
     * @return
     */
    @Override
    public R<RpcStatus> dropPartition(String collectionName, String partitionName) {
        R<RpcStatus> response = milvusServiceClient.dropPartition(
                DropPartitionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withPartitionName(partitionName)
                        .build());
        return response;
    }


}
