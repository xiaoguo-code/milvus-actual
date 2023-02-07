package com.gyr.milvusactual.controller.page;

import com.gyr.milvusactual.common.result.Result;
import com.gyr.milvusactual.common.result.ResultCodeEnum;
import com.gyr.milvusactual.common.util.ByteUtils;
import com.gyr.milvusactual.common.util.FileUtil;
import com.gyr.milvusactual.controller.AlbumController;
import com.gyr.milvusactual.controller.FileController;
import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.service.FaceEngineService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping("/indexHtml")
    public String getDemoHtml(Model model) {

        //此处是需要展示的html在templates下的具体路径
        return "index";
    }

    @RequestMapping("/search")
    public String search(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("collection") String collection,
            @RequestParam("score") double score,
            Model model
    ) throws IOException {
        File file = FileUtil.multipartFileToFile(multipartFile);
        //获取特征值
        byte[] bytes = faceEngineService.faceFindCropFeature(file);
        //矢量检索
        List<List<Float>> searchVectors = new ArrayList<>();
        List<Float> vectors = ByteUtils.byteArray2List(bytes);
        searchVectors.add(vectors);
        List<?> list = vectorDbService.searchByFeature(collection, searchVectors);
        Result ok = Result.ok(ResultCodeEnum.SUCCESS);
        for (Object featureKey : list) {
            String featureKeyStr = String.valueOf(featureKey);
            if (FileController.imgRepositoryMap.containsKey(featureKeyStr)) {
                ok.data(featureKeyStr, FileController.imgRepositoryMap.get(featureKeyStr));
            }
        }
        log.info("查询数量:{}",ok.getData().size());
        model.addAttribute("dataMap", ok.getData());
        return "show";
    }

}
