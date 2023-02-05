package com.gyr.milvusactual;

import com.gyr.milvusactual.dao.VectorDbService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @desc:
 * @Author: guoyr
 * @Date: 2022-02-14 23:01
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MilvusActualApplication.class})
public class DataDeleteOperationTest {


    @Autowired
    private VectorDbService milvusService;


    @Autowired
    private MilvusServiceClient milvusServiceClient;


    final String collectionName = "passerby_20220219";

    /**
     * 删除集合
     */
    @Test
    public void dropCollection() {
        R<RpcStatus> response = milvusService.collectionManage().dropCollection(collectionName);
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
