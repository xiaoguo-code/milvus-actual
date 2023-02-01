package com.gyr.milvusactual;

import cn.hutool.json.JSONUtil;
import com.gyr.milvusactual.dao.MilvusService;
import com.gyr.milvusactual.entity.Passerby;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.partition.CreatePartitionParam;
import io.milvus.param.partition.HasPartitionParam;
import io.milvus.response.GetCollStatResponseWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @desc:
 * @Author: guoyr
 * @Date: 2022-02-14 23:01
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MilvusActualApplication.class})
public class DataDeleteOperationTest {


    @Autowired
    private MilvusService milvusService;


    @Autowired
    private MilvusServiceClient milvusServiceClient;


    final String collectionName = "passerby_20220219";

    /**
     * 删除集合
     */
    @Test
    public void dropCollection() {
        R<RpcStatus> response = milvusService.CollectionManage().dropCollection(collectionName);
        System.out.println("删除集合，response：" + response.toString());

    }

    @Test
    public void dropPartition() {

        final String partitionName = "HC";

        R<RpcStatus> response = milvusService.partitionManage().dropPartition(collectionName, partitionName);
        System.out.println("删除分区，response：" + response.toString());


    }


    /**
     * 删除数据
     */
    @Test
    public void dataDrop() {

        String DELETE_EXPR = "face_id in [1,6,8] ";

        //删除后为啥集合大小不变呢？但是查询查不到了？  为了方便回滚，数据被保留一段时间
        R<MutationResult> response = milvusService.dataManage().delete(collectionName, DELETE_EXPR);

        System.out.println("删除数据，response：" + response.toString());


    }


}
