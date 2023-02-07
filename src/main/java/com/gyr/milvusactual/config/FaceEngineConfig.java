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

//    public static String appId;
//    public static String sdkKey;
//    public static String lib;
//    public static String testImgOne;
//    public static String testImgTwo;
//
//    public void setAppId(String appId) {
//        FaceEngineConfig.appId = appId;
//    }
//
//    public void setSdkKey(String sdkKey) {
//        FaceEngineConfig.sdkKey = sdkKey;
//    }
//
//    public void setLib(String lib) {
//        FaceEngineConfig.lib = lib;
//    }
//
//    public void setTestImgOne(String testImgOne) {
//        FaceEngineConfig.testImgOne = testImgOne;
//    }
//
//    public void setTestImgTwo(String testImgTwo) {
//        FaceEngineConfig.testImgTwo = testImgTwo;
//    }
}
