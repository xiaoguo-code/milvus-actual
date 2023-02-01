package com.gyr.milvusactual.dao.impl;


import com.gyr.milvusactual.dao.DataManageService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.alias.AlterAliasParam;
import io.milvus.param.alias.CreateAliasParam;
import io.milvus.param.alias.DropAliasParam;
import io.milvus.param.collection.*;
import io.milvus.param.control.GetCompactionStateParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.GetCollStatResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataManageServiceImpl implements DataManageService {


    /**
     * 注入milvus连接客户端
     */
    @Autowired
    private MilvusServiceClient milvusServiceClient;


    /**
     * 插入数据
     *
     * @param insertParam
     * @return
     */
    @Override
    public R<MutationResult> insert(InsertParam insertParam) {
        R<MutationResult> response = milvusServiceClient.insert(insertParam);
        return response;
    }

    /**
     * 删除数据
     *
     * @param collectionName
     * @param deleteExpr
     * @return
     */
    @Override
    public R<MutationResult> delete(String collectionName, String deleteExpr) {
        R<MutationResult> response = milvusServiceClient.delete(
                DeleteParam.newBuilder()
                        .withCollectionName(collectionName)
//                        .withPartitionName("HC")
                        .withExpr(deleteExpr)
                        .build());
        return response;
    }

    /**
     * 压缩集合
     *
     * @param collectionName
     * @return
     */
    @Override
    public R<ManualCompactionResponse> manualCompaction(String collectionName) {
//        R<ManualCompactionResponse> response = milvusServiceClient.manualCompaction(
//                ManualCompactionParam.newBuilder()
//                        .withCollectionName(collectionName)
//                        .build());
//        long compactionID = response.getData().getCompactionID();
//        return response;
        return null;
    }

    /**
     * 检查压缩状态
     *
     * @param compactionID
     * @return
     */
    @Override
    public R<GetCompactionStateResponse> getCompactionState(long compactionID) {
        R<GetCompactionStateResponse> response = milvusServiceClient.getCompactionState(GetCompactionStateParam.newBuilder()
                .withCompactionID(compactionID)
                .build());
        return response;
    }
}
