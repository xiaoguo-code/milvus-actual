package com.gyr.milvusactual;

import cn.hutool.core.io.FileUtil;
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
import com.gyr.milvusactual.service.FaceEngineService;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @desc:
 * @Author: guoyr
 * @Date: 2023-02-08 21:20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MilvusActualApplication.class})
@Slf4j
public class InitLocalImgDataTest {


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private VectorDbService vectorDbService;

    public static final String ID = "_id";
    public static final String collectionName = "face_album";


    @Autowired
    private FaceEngineService faceEngineService;


    @Test
    public void init() {
        List<String> fileNames = FileUtil.listFileNames(AlbumCollectionConfig.IMG_PATH);

        for (String fileName : fileNames) {
            String filePath = AlbumCollectionConfig.IMG_PATH + File.separator + fileName;
            File file = FileUtil.file(filePath);
            // 设置雪花算法设置key
            SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(0, 0);
            String id = snowflakeIdWorker.nextId() + "";
            //人脸结构化
            byte[] bytes = faceEngineService.faceFindCropFeature(file);
            if (bytes == null || bytes.length <= 0) {
                continue;
            }
            List<Float> vector = ByteUtils.byteArrayToFloatList(bytes);
            List<List<Float>> floatArrayList = new ArrayList<>();
            floatArrayList.add(vector);
            //入milvus
            long startTime = System.currentTimeMillis();
            Long insert = vectorDbService.insert(AlbumCollectionConfig.COLLECTION_NAME,
                    AlbumCollectionConfig.getRandomPartitionName(),
                    Lists.newArrayList(Long.valueOf(id)),
                    Lists.newArrayList(id),
                    floatArrayList);
            long consume = System.currentTimeMillis() - startTime;
            if (insert != null) {
                log.info("集合:{}-id:{},特征入库成功!耗时:{}", AlbumCollectionConfig.COLLECTION_NAME, id, consume);
            } else {
                log.error("集合:{}-id:{},特征入库失败!耗时:{}", AlbumCollectionConfig.COLLECTION_NAME, id, consume);
            }
            //id、文件路径入mongo
            FaceInfo info = new FaceInfo();
            info.set_id(id);
            String replace = filePath.replace(AlbumCollectionConfig.IMG_PATH, "");
            info.setImageUrl("/img" + replace);
            FaceInfo insert1 = mongoTemplate.insert(info, collectionName);
            log.info("集合:{}-id:{},mongo入库成功:{}", collectionName, id, JSONUtil.toJsonStr(insert1));

        }

    }

}
