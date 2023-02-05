package com.gyr.milvusactual;

import com.gyr.milvusactual.util.FaceEngineTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
public class MilvusActualApplication {

    public static void main(String[] args) {
        FaceEngineTest.engineTest();
        System.out.println("======================================");
        SpringApplication.run(MilvusActualApplication.class, args);
    }


}
