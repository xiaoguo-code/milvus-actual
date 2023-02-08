package com.gyr.milvusactual;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gyr.milvusactual.common.util.ByteUtils;
import com.gyr.milvusactual.common.util.ImageUtil;
import com.gyr.milvusactual.common.util.SnowflakeIdWorker;
import com.gyr.milvusactual.config.AlbumCollectionConfig;
import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.entity.FaceInfo;
import com.gyr.milvusactual.entity.myengine.DetectInfoOptionVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @author guoyr
 * @description
 * @date 2023/2/8 14:44
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MilvusActualApplication.class})
@Slf4j
public class InitPasserbyDataTest {


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private VectorDbService vectorDbService;

    public static final String ID = "_id";
    public static final String collectionName = "passerby_20230208";
    public static final String option = "{\"Detection\": [\"FaceDetection\"],\"FaceRecognition\": [\"FaceFeature\"]}";
    public static final String detectUrl = "http://10.30.85.11:8083/future/api/mia/general/getAllDetectFormat";



    @Test
    public void init() {
        int limit = 10;
        int skip = 0;
        int total = 0;
        while (true) {
            Query query = Query.query(new Criteria());
//            query.with(Sort.by(Sort.Direction.ASC, ID));
            query.skip(skip);
            query.limit(limit);
            List<FaceInfo> objectList = mongoTemplate.find(query, FaceInfo.class, collectionName);
            CountDownLatch countDownLatch = new CountDownLatch(objectList.size());
            for (FaceInfo info : objectList) {
                CompletableFuture.runAsync(() -> {
                    createFace(info);
                    countDownLatch.countDown();
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            total += objectList.size();
            log.info("{}清洗进度:total:{}", collectionName, total);
            if (objectList.size() == 0 || objectList.size() % limit != 0) {
                log.info("{}表清洗完成", collectionName);
                break;
            }
            skip += 10;

        }
        log.info("{}总数据:{}", collectionName, total);
    }

    private void createFace(FaceInfo info) {
        //根据url获取base64
        String base64FromUrl = ImageUtil.getBase64FromUrl(info.getImageUrl());
        //调用单引擎结构化，获取特征值
        String featureStr = "";
        DetectInfoOptionVo detectInfoOptionVo = new DetectInfoOptionVo();
        detectInfoOptionVo.setOptions(option);
        detectInfoOptionVo.setImageBase64(base64FromUrl);
        detectInfoOptionVo.setUseNewFormat(true);
        String post = HttpUtil.post(detectUrl, JSONUtil.toJsonStr(detectInfoOptionVo));
        JSONObject jsonResultObj = JSONUtil.parseObj(post);
        JSONObject dataJsonObj = jsonResultObj.getJSONObject("data");
        JSONObject resultObjectJsonObj = dataJsonObj.getJSONObject("ResultObject");
        if (resultObjectJsonObj == null) {
            return;
        }
        JSONArray imageListJsonArr = resultObjectJsonObj.getJSONArray("ImageList");
        if (imageListJsonArr == null || imageListJsonArr.size() <= 0) {
            return;
        }
        for (Object imageObj : imageListJsonArr) {
            JSONObject imageJsonObj = (JSONObject) imageObj;
            JSONArray faceListJsonArr = imageJsonObj.getJSONArray("FaceList");
            if (faceListJsonArr == null || faceListJsonArr.size() <= 0) {
                continue;
            }
            for (Object faceObj : faceListJsonArr) {
                JSONObject faceJsonObj = (JSONObject) faceObj;
                JSONObject attr = faceJsonObj.getJSONObject("Attr");
                if (attr == null) {
                    continue;
                }
                featureStr = attr.getStr("Feat");
            }
        }
        if (StringUtils.isBlank(featureStr)) {
            return;
        }
        SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(0, 0);
        String featureId = snowflakeIdWorker.nextId() + "";
        List<List<Float>> floatArrayList = new ArrayList<>();
        List<Float> floats = ByteUtils.byteArrayToFloatList(Base64Utils.decodeFromString(featureStr));
        floatArrayList.add(floats);
        long startTime = System.currentTimeMillis();
        //入milvus
        Long insert = vectorDbService.insert(AlbumCollectionConfig.COLLECTION_NAME,
                AlbumCollectionConfig.getRandomPartitionName(),
                Lists.newArrayList(Long.valueOf(featureId)),
                Lists.newArrayList(info.get_id()),
                floatArrayList);
        long consume = System.currentTimeMillis() - startTime;
        if (insert != null) {
            log.info("集合:{}-id:{}特征入库成功!耗时:{}", AlbumCollectionConfig.COLLECTION_NAME, info.get_id(), consume);
        } else {
            log.error("集合:{}-id:{}特征入库失败!耗时:{}", AlbumCollectionConfig.COLLECTION_NAME, info.get_id(), consume);
        }
    }


}
