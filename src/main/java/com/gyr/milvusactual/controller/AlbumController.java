package com.gyr.milvusactual.controller;


import com.gyr.milvusactual.common.result.Result;
import com.gyr.milvusactual.common.result.ResultCodeEnum;
import com.gyr.milvusactual.common.util.ByteUtils;
import com.gyr.milvusactual.common.util.FileUtil;
import com.gyr.milvusactual.config.AlbumCollectionConfig;
import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.entity.FaceInfo;
import com.gyr.milvusactual.service.FaceEngineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/album")
@Slf4j
public class AlbumController {

    @Autowired
    VectorDbService vectorDbService;

    @Autowired
    FaceEngineService faceEngineService;
    @Autowired
    MongoTemplate mongoTemplate;

    @PostMapping("/create")
//    @ApiOperation(value = "创建底库")
    public Result create(@RequestParam("collection") String collection) {
        if (StringUtils.isBlank(collection)) {
            return Result.error(ResultCodeEnum.PARAM_ERROR);
        }
        if (!vectorDbService.createCollection(collection)) {
            return Result.error(ResultCodeEnum.UNKNOW_REASON).data("message", "创建集合失败");
        }
        for (Integer i = 0; i < AlbumCollectionConfig.PARTITION_NUM; i++) {
            if (!vectorDbService.createPartition(collection, AlbumCollectionConfig.getPartitionName(i))) {
                return Result.error(ResultCodeEnum.UNKNOW_REASON).data("message", "创建分区失败");
            }
        }
        return Result.ok(ResultCodeEnum.SUCCESS);
    }

    @PostMapping("/delete")
//    @ApiOperation(value = "删除底库")
    public Result delete(@RequestParam("collection") String collection) {
        if (StringUtils.isBlank(collection)) {
            return Result.error(ResultCodeEnum.PARAM_ERROR);
        }
        if (!vectorDbService.dropCollection(collection)) {
            return Result.error(ResultCodeEnum.UNKNOW_REASON).data("message", "删除集合失败");
        }
        return Result.ok(ResultCodeEnum.SUCCESS);
    }

    @PostMapping("/search")
//    @ApiOperation(value = "底库1:N")
    public Result search(
            @RequestPart("file") MultipartFile multipartFile,
            @RequestParam(value = "collection", required = false) String collection,
            @RequestParam(value = "score", required = false) Double score
    ) throws IOException {
        File file = FileUtil.multipartFileToFile(multipartFile);
        //获取特征值
        byte[] bytes = faceEngineService.faceFindCropFeature(file);
        //矢量检索
        List<List<Float>> searchVectors = new ArrayList<>();
        List<Float> vectors = ByteUtils.byteArrayToFloatList(bytes);
        searchVectors.add(vectors);
        List<?> list = vectorDbService.searchByFeature(AlbumCollectionConfig.COLLECTION_NAME, searchVectors);
        Criteria criteria = Criteria.where("_id").in(list);
        Query query = Query.query(criteria);
        List<FaceInfo> faceInfos = mongoTemplate.find(query, FaceInfo.class, "face_album");
        List<FaceInfo> result = new ArrayList<>();
        for (Object id : list) {
            for (FaceInfo faceInfo : faceInfos) {
                if (faceInfo.get_id().equals(id)) {
                    result.add(faceInfo);
                }
            }

        }
        log.info("查询数量:{}", result.size());
        return Result.ok(ResultCodeEnum.SUCCESS).data("data",result);
    }

}
