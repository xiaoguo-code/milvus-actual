package com.gyr.milvusactual.pool;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.*;

/**
 * @author lanxp
 * @Desc 线程池构建类
 * @create 2020-04-23 19:38
 */
public class MyThreadPool {

    public static ThreadPoolExecutor myNewThreadPool(int poolSize) {
        return myNewThreadPool(poolSize, null);
    }

    public static ThreadPoolExecutor myNewThreadPool(int poolSize, String poolName) {
        return myNewThreadPool(poolSize, Integer.MAX_VALUE, poolName);
    }

    public static ThreadPoolExecutor myNewThreadPool(int poolSize, int queueCapacity, String poolName) {
        return myNewThreadPool(poolSize, poolSize, queueCapacity, poolName);
    }

    public static ThreadPoolExecutor myNewThreadPool(int corePoolSize, int maximumPoolSize, int queueCapacity, String poolName) {
        return myNewThreadPool(corePoolSize, maximumPoolSize, 30L, queueCapacity, poolName);
    }

    /**
     * 最终创建实现
     *
     * @param corePoolSize    核心线程
     * @param keepAliveTime   存活时间
     * @param maximumPoolSize 最大线程
     * @param queueCapacity   队列长度
     * @param poolName        线程名称
     * @return
     * @author lanxp
     * @Date 2020/4/27 11:36
     */
    public static ThreadPoolExecutor myNewThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity, String poolName) {
        ThreadFactory threadFactory = null;
        if (keepAliveTime == 0L) {
            keepAliveTime = 30L;
        }
        if (StringUtils.isNotBlank(poolName)) {
            threadFactory = new MyThreadFactory(poolName);
        } else {
            threadFactory = Executors.defaultThreadFactory();
        }

        //创建线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(queueCapacity), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());

        //设置核心线程可以销毁
        threadPoolExecutor.allowCoreThreadTimeOut(true);

        return threadPoolExecutor;
    }
}
