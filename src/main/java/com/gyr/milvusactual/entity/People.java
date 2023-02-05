package com.gyr.milvusactual.entity;

import lombok.Data;

import java.util.List;

@Data
public class People {

    private long id;

    private String name;

    private List<Float> feature;

    private String partition;
}
