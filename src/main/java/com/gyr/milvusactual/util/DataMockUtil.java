package com.gyr.milvusactual.util;


import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.gyr.milvusactual.config.AlbumCollectionConfig;
import com.gyr.milvusactual.entity.People;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataMockUtil {


    public List<People> mockPeople(Integer num) {
        List<People> peopleList = new ArrayList<>();
        for (int i = 0; i < num; ++i) {
            People people = new People();
            people.setId((long) i);
            people.setName("name" + i);
            //[经度，维度，性别，年龄，身高，体重]
            people.setFeature(Lists.newArrayList(mockLonLat(90), mockLonLat(180), mockGender(2), mockAge(100), mockHeight(200), mockWeight(200)));
            people.setPartition(AlbumCollectionConfig.getPartitionName(i));
            peopleList.add(people);
        }
        return peopleList;
    }

    private float mockLonLat(Integer limit) {
        Random random = new Random();
        int a = random.nextInt(limit);
        float b = random.nextFloat();
        BigDecimal bd = new BigDecimal(b);
        float b2 = bd.setScale(6, BigDecimal.ROUND_HALF_EVEN).floatValue();
        return a + b2;
    }

    private float mockGender(Integer limit) {
        Random random = new Random();
        int a = random.nextInt(limit);
        return a + 1;
    }

    private float mockAge(Integer limit) {
        Random random = new Random();
        int a = random.nextInt(limit);
        return a;
    }

    private float mockHeight(Integer limit) {
        Random random = new Random();
        int a = random.nextInt(limit);
        float b = random.nextFloat();
        BigDecimal bd = new BigDecimal(b);
        float b2 = bd.setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue();
        return a + b2;
    }

    private float mockWeight(Integer limit) {
        Random random = new Random();
        int a = random.nextInt(limit);
        float b = random.nextFloat();
        BigDecimal bd = new BigDecimal(b);
        float b2 = bd.setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue();
        return a + b2;
    }

    public static void main(String[] args) {
        DataMockUtil dataMockUtil = new DataMockUtil();
        List<People> peoples = dataMockUtil.mockPeople(100);
        for (People people : peoples) {
            System.out.println(JSONUtil.toJsonStr(people));
        }
    }

}
