package com.gyr.milvusactual.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * ClassName:SwaggerConfig
 * Package:com.guoyr.cloud.article.config
 * Description:
 *
 * @date:2022/11/28 11:26 上午
 * @author:guoyr
 */
@Configuration
//@EnableSwagger2 //swagger3版本不需要使用这个注解，当然写上也无所谓~
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)  // swagger2版本这里是DocumentationType.SWAGGER_2
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build();
    }


    /**
     * api信息描述
     * @return apiInfo
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("xxx管理平台")                       //标题
                .description("xxx管理平台 API接口文档")      //简介
                .license("xxx有限公司")
                .licenseUrl("xxx")
                .version("1.0")                          //版本
                .build();
    }
}
