package com.gyr.milvusactual.client;


import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * milvus初始化客户端   搜索引擎，检索相似
 */
@Configuration
public class MyMilvusClient {


    @Value("${database.milvus.host:10.30.30.31}")
    private String host;

    @Value("${database.milvus.port:19530}")
    private Integer port;


    /**
     * 初始化MilvusServiceClient，并由spring管理
     *
     * @return
     */
    @Bean
    public MilvusServiceClient getMilvusServiceClient() {

        MilvusServiceClient milvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(host)
                        .withPort(port)
                        .build());

        return milvusClient;
    }


}
