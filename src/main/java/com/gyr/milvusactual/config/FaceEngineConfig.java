package com.gyr.milvusactual.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "engine.arc-soft")
public class FaceEngineConfig {

    public String appId;
    public String sdkKey;
    public String lib;
    public String testImgOne;
    public String testImgTwo;

}
