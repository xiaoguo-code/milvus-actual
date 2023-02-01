/**
 * @(#) ContextLoader.java
 * @Package com.meiya.engine
 * <p>
 * Copyright © Meiya Corporation. All rights reserved.
 */

package com.gyr.milvusactual.pool;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池管理
 */
@Configuration
public class ThreadPoolContextManager {

    /**
     * 入库线程池
     */
    public static ThreadPoolExecutor insertThreadPool;
    /**
     * 入库线程池队列
     */
    public static BlockingQueue<Runnable> insertPoolQueue;


    public static Map<String, ThreadPoolExecutor> threadPoolMap = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {

        insertThreadPool = MyThreadPool.myNewThreadPool(100,
                200,500,"insertThreadPool");

        insertPoolQueue = insertThreadPool.getQueue();

        threadPoolMap.put("insertThreadPool", insertThreadPool);
    }
}
