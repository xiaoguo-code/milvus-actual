package com.gyr.milvusactual.common.milvus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ClassName:MilvusServerInfo
 * Package:com.gyr.milvusactual.common.milvus
 * Description:
 *
 * @date:2023/2/6 12:44 上午
 * @author:guoyr
 */
@Component
public class MilvusServerInfo {
    /**
     * milvus所在服务器地址
     */

    public static String HOST;

    /**
     * milvus端口
     */
    public static Integer PORT;

    @Value("${spring.data.milvus.host:}")
    public void setHost(String host) {
        MilvusServerInfo.HOST = host;
    }

    @Value("${spring.data.milvus.port:19530}")
    public void setPort(Integer port) {
        MilvusServerInfo.PORT = port;
    }
}
