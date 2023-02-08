package com.gyr.milvusactual.controller.page;

import com.gyr.milvusactual.common.result.Result;
import com.gyr.milvusactual.common.result.ResultCodeEnum;
import com.gyr.milvusactual.common.util.ByteUtils;
import com.gyr.milvusactual.common.util.FileUtil;
import com.gyr.milvusactual.config.AlbumCollectionConfig;
import com.gyr.milvusactual.controller.AlbumController;
import com.gyr.milvusactual.controller.FileController;
import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.entity.FaceInfo;
import com.gyr.milvusactual.service.FaceEngineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @desc:
 * @Author: guoyr
 * @Date: 2023-02-07 22:31
 */
@Controller
@Slf4j
public class IndexController {

    @Autowired
    VectorDbService vectorDbService;

    @Autowired
    FaceEngineService faceEngineService;

    @Autowired
    MongoTemplate mongoTemplate;

    @RequestMapping("/indexHtml")
    public String getDemoHtml(Model model) {

        //此处是需要展示的html在templates下的具体路径
        return "index";
    }

    @RequestMapping("/search")
    public String search(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam(value = "collection", required = false) String collection,
            @RequestParam(value = "score", required = false) Double score,
            Model model
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
        log.info("查询数量:{}", faceInfos.size());
        model.addAttribute("dataList", faceInfos);
        return "show";
    }

}
