package com.gyr.milvusactual.dao;

import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.grpc.ShowPartitionsResponse;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.GetCollStatResponseWrapper;

/**
 * 分区管理
 */
public interface PartitionManageService {


    /**
     * 创建分区
     *
     * @param collectionName
     * @param partitionName
     * @return
     */
    R<RpcStatus> createPartition(String collectionName, String partitionName);


    /**
     * 验证分区是否存在
     *
     * @param collectionName
     * @param partitionName
     * @return
     */
    R<Boolean> hasPartition(String collectionName, String partitionName);


    /**
     * 获取集合的所有分区
     *
     * @param collectionName
     * @return
     */
    R<ShowPartitionsResponse> showPartitions(String collectionName);

    /**
     * 删除指定集合中的分区
     *
     * @param collectionName
     * @param partitionName
     * @return
     */
    R<RpcStatus> dropPartition(String collectionName, String partitionName);
}
