package com.gyr.milvusactual.dao.milvus;

import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.GetCollStatResponseWrapper;

/**
 * 分区管理
 */
public interface IndexsManageService {

    /**
     * 创建索引
     * @param collectionName 集合名
     * @return true or false
     */
    Boolean createIndex(String collectionName);

}
