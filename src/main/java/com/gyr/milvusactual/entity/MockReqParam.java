package com.gyr.milvusactual.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author guoyr
 * @description 模拟数据请求参数
 * @date 2022/2/18 9:57
 */
@Data
public class MockReqParam implements Serializable {

    private Integer dim;

    private Long num;

    private Long addNum;

    private Long gridNum;

    private String collectionName;

    private String partitionName;

}
