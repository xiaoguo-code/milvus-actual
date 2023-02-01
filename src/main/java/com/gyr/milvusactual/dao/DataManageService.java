package com.gyr.milvusactual.dao;

import io.milvus.grpc.GetCompactionStateResponse;
import io.milvus.grpc.ManualCompactionResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.dml.InsertParam;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.GetCollStatResponseWrapper;

/**
 * 数据管理
 */
public interface DataManageService {


    /**
     * 插入数据
     *
     * @param insertParam
     * @return
     */
    R<MutationResult> insert(InsertParam insertParam);

    /**
     * 删除数据
     * @param collectionName
     * @param deleteExpr
     * @return
     */
    R<MutationResult> delete(String collectionName, String deleteExpr);


    /*
      Milvus 默认支持自动数据压缩。您可以配置您的 Milvus 以启用或禁用压缩和自动压缩。

      如果禁用自动压缩，您仍然可以手动压缩数据。*/

    /**
     * 压缩集合
     *
     * @param collectionName
     * @return
     */
    R<ManualCompactionResponse> manualCompaction(String collectionName);

    /**
     * 检查压缩状态
     *
     * @param compactionID
     * @return
     */
    R<GetCompactionStateResponse> getCompactionState(long compactionID);


}
