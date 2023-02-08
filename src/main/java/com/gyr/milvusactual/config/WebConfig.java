package com.gyr.milvusactual.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @desc:
 * @Author: guoyr
 * @Date: 2023-02-07 23:10
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    /*
     *addResourceHandler:访问映射路径
     *addResourceLocations:资源绝对路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/**").addResourceLocations("file:/C:/Users/41071/Pictures/img/");
    }

}
