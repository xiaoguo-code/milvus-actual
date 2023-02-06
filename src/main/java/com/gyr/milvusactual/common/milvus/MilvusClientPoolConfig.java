package com.gyr.milvusactual.common.milvus;


import com.arcsoft.face.FaceEngine;
import com.gyr.milvusactual.common.factory.MilvusPoolFactory;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * milvus初始化客户端   搜索引擎，检索相似
 */
@Configuration
@Slf4j
public class MilvusClientPoolConfig {

    @Bean
    public GenericObjectPool<MilvusServiceClient> getMilvusServiceClient() {
// 私有构造方法创建一个池
        // 对象池工厂
        MilvusPoolFactory milvusPoolFactory = new MilvusPoolFactory();
        // 对象池配置
        GenericObjectPoolConfig<FaceEngine> objectPoolConfig = new GenericObjectPoolConfig<>();
        objectPoolConfig.setMaxTotal(8);
        objectPoolConfig.setJmxEnabled(false);
        AbandonedConfig abandonedConfig = new AbandonedConfig();

        abandonedConfig.setRemoveAbandonedOnMaintenance(true); //在Maintenance的时候检查是否有泄漏

        abandonedConfig.setRemoveAbandonedOnBorrow(true); //borrow 的时候检查泄漏

        // https://milvus.io/cn/docs/v2.0.x/load_collection.md
        int MAX_POOL_SIZE = 5;
        abandonedConfig.setRemoveAbandonedTimeout(MAX_POOL_SIZE); //如果一个对象borrow之后10秒还没有返还给pool，认为是泄漏的对象

        // 管理链接对象的池子
        GenericObjectPool<MilvusServiceClient> milvusServiceClientGenericObjectPool = new GenericObjectPool(milvusPoolFactory, objectPoolConfig);
        milvusServiceClientGenericObjectPool.setAbandonedConfig(abandonedConfig);
        milvusServiceClientGenericObjectPool.setTimeBetweenEvictionRunsMillis(5000); //5秒运行一次维护任务
        log.info("milvus 对象池创建成功 维护了" + MAX_POOL_SIZE + "个对象");
        return milvusServiceClientGenericObjectPool;
    }

}
