package com.gyr.milvusactual.common.factory;

import com.gyr.milvusactual.common.milvus.MilvusServerInfo;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * milvus客户端对象池工厂
 */
@Component
public class MilvusPoolFactory extends BasePooledObjectFactory<MilvusServiceClient> {


    @Override
    public MilvusServiceClient create() throws Exception {
        return new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(MilvusServerInfo.HOST)
                        .withPort(MilvusServerInfo.PORT)
                        .build());
    }

    @Override
    public PooledObject<MilvusServiceClient> wrap(MilvusServiceClient milvusServiceClient) {
        return new DefaultPooledObject<>(milvusServiceClient);

    }
}
