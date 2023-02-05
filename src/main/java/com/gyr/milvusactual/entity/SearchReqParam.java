package com.gyr.milvusactual.entity;

import io.milvus.param.MetricType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author guoyr
 * @description 模拟数据请求参数
 * @date 2022/2/18 9:57
 */
@Data
public class SearchReqParam implements Serializable {


    private String collectionName;

    private List<String> partitionName;

    private String exr;

    private String vectorFieldName;

    private List<String> searchOutputFields;

    private Integer topK;

    private List<List<Float>> featureVector;

    private MetricType metricType;

    private String searchParam;



}
