package com.gyr.milvusactual.dao.milvus;

import io.milvus.grpc.GetCompactionStateResponse;
import io.milvus.grpc.ManualCompactionResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.dml.InsertParam;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.GetCollStatResponseWrapper;

import java.util.List;

/**
 * 数据管理
 */
public interface DataManageService {


    /**
     * 矢量搜索
     *
     * @param collection
     * @param search_vectors
     * @return
     */
    public List<?> searchByFeature(String collection, List<List<Float>> search_vectors);

    /**
     * 插入数据
     *
     * @param collectionName
     * @param partitionName
     * @param ids
     * @param name
     * @param feature
     * @return
     */
    Long insert(String collectionName, String partitionName, List<Long> ids, List<String> name, List<List<Float>> feature);

    /**
     * 删除数据
     *
     * @param collectionName
     * @param deleteExpr
     * @return
     */
    Boolean delete(String collectionName, String deleteExpr);


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
    GetCompactionStateResponse getCompactionState(long compactionID);


}
