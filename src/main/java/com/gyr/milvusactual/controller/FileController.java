package com.gyr.milvusactual.controller;

import cn.hutool.json.JSON;
import com.arcsoft.face.FaceInfo;
import com.gyr.milvusactual.common.face.FaceEngineUtil;
import com.gyr.milvusactual.common.result.Result;
import com.gyr.milvusactual.common.result.ResultCodeEnum;
import com.gyr.milvusactual.common.util.Base64Utils;
import com.gyr.milvusactual.common.util.ByteUtils;
import com.gyr.milvusactual.common.util.SnowflakeIdWorker;
import com.gyr.milvusactual.common.util.ZipUtils;
import com.gyr.milvusactual.config.AlbumCollectionConfig;
import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.service.FaceEngineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/file")
//@Tag(name = "文件上传接口")
@Slf4j
public class FileController {
    /**
     * 文件上传后的存储位置
     */
    @Value("${uploadFile.tempLocation:/Users/admin/Documents/idea/code/milvus-actual/img}")
    String tempFilepath;

    public static Map<String, String> imgRepositoryMap = new HashMap<>();

    @Autowired
    FaceEngineUtil faceEngineUtil;

    @Autowired
    VectorDbService vectorDbService;

    @Autowired
    private FaceEngineService faceEngineService;

//    @Autowired
//    faceList faceList;

//    @Autowired
//    RedisService redisService;


    @PostMapping("/getImageUrl")
//    @Operation(summary = "获取照片URL")
    public Result getImageUrl(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.ok(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }
        try {
            InputStream inputStream = file.getInputStream(); // 上传的照片获取流
            InputStream inputStreamFeature = file.getInputStream(); // 上传的照片获取流
            List<FaceInfo> faceInfos = faceEngineUtil.faceFind(inputStream);
            if (faceInfos.size() == 0) {
                return Result.ok(ResultCodeEnum.NO_FACE);
            }
            if (faceInfos.size() > 1) {
                return Result.ok(ResultCodeEnum.HAVE_MORE_FACE);
            } else {
                // 照片裁剪
                String encoding = faceEngineUtil.faceCrop(file.getInputStream(), faceInfos.get(0).getRect());
                if (encoding == null || "".equals(encoding)) {
                    return Result.ok(ResultCodeEnum.NO_FACE);
                }
                // 进行特征值提取
                byte[] feature = faceEngineUtil.faceFeature(inputStreamFeature, faceInfos.get(0));
                if (feature.length == 0) {
                    return Result.ok(ResultCodeEnum.HAVE_MORE_FACE);
                }
                // 设置雪花算法设置key
                SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(0, 0);
                String featureKey = snowflakeIdWorker.nextId() + ""; // redis中存储的key
                String featureStr = Base64Utils.byteArray2Base(feature);
                return Result.ok(ResultCodeEnum.HAVE_FACE).data("feature", featureStr);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Result.error(ResultCodeEnum.UNKNOW_REASON).data("message", e.getMessage());
        }
    }

    @PostMapping("/initAlbumBatch")
//    @Operation(summary = "批量上传照片")
    public Result getListImageUrl(@RequestPart("file") MultipartFile file, @RequestParam("collection") String collection) {
        if (StringUtils.isBlank(collection)) {
            Result.error(ResultCodeEnum.PARAM_ERROR).data("message", "请指定库名");
        }
        if (!file.isEmpty()) {
            String uploadPath = tempFilepath + File.separator + UUID.randomUUID().toString();
            // 如果目录不存在则创建
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String OriginalFilename = file.getOriginalFilename();//获取原文件名
            String suffixName = OriginalFilename.substring(OriginalFilename.lastIndexOf("."));//获取文件后缀名
            String uploadFileName = OriginalFilename.replace(suffixName, "");
            if (!suffixName.endsWith(".zip")) {
                return Result.error(ResultCodeEnum.FILE_UPLOAD_ERROR).data("message", "请上传zip包");
            }
            //重新随机生成名字
            String filename = UUID.randomUUID().toString() + suffixName;
            File localFile = new File(uploadPath + File.separator + filename);
            try {
                file.transferTo(localFile); //把上传的文件保存至本地
                /**
                 * 这里应该把filename保存到数据库,供前端访问时使用
                 */
                Map<String, String> successList = new HashMap<>();
                Map<String, String> failedList = new HashMap<>();
                try {
                    ZipUtils.zipUncompress(uploadPath + File.separator + filename, uploadPath);  // windows 用// linux 用 /
                    File photoList = new File(uploadPath + File.separator + uploadFileName);
                    log.info(uploadPath + File.separator + uploadFileName);
                    File[] listFiles = photoList.listFiles();
                    faceFileBatchInsert(collection, listFiles, successList, failedList);
                } catch (Exception e) {
                    log.error("文件解压失败", e);
                    return Result.error();
                }
                return Result.ok()
                        .data("path", uploadPath + File.separator + filename)
                        .data("success", successList)
                        .data("failed", failedList);//上传成功，返回保存的文件地址
            } catch (IOException e) {
                log.error("上传失败", e);
                return Result.error();
            }
        } else {
            log.error("文件为空");
            return Result.error();
        }

    }

    private Result faceFileBatchInsert(String collection, File[] listFiles, Map<String, String> successList, Map<String, String> failedList) {
        for (int i = 0; i < listFiles.length; i++) {
            File file = listFiles[i];
            String name = file.getName();
            String suffixName = name.substring(name.lastIndexOf("."));
            String fileName = name.replace(suffixName, "");
            // 开始处理人脸信息
            try {
                SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(0, 0);
                String featureKey = snowflakeIdWorker.nextId() + "";
                byte[] feature = faceEngineService.faceFindCropFeature(file);
                if (feature == null) {
                    failedList.put(name, "特征提取失败");
                    return Result.error(ResultCodeEnum.NO_FACE).data("message", "特征提取失败");
                }
                List<Long> ids = new ArrayList<>();
                List<String> featureKeys = new ArrayList<>();
                List<List<Float>> features = new ArrayList<>();
                ids.add((long) i);
                featureKeys.add(featureKey);
                features.add(ByteUtils.byteArray2List(feature));
                Long insert = vectorDbService.insert(collection, AlbumCollectionConfig.getPartitionName(i), ids, featureKeys, features);
                if (insert != null) {
                    log.info("file:{},特征值入库成功", name);
                    successList.put(name, "特征值入库成功");
                    imgRepositoryMap.put(featureKey, "/img" + file.getAbsolutePath().replace(tempFilepath, ""));
                } else {
                    log.info("file:{},特征值入库失败", name);
                    failedList.put(name, "特征值入库失败");
                }
            } catch (Exception e) {
                log.error("批量插入发生异常", e);
                failedList.put(name, "批量插入发生异常");
                return Result.error(ResultCodeEnum.UNKNOW_REASON).data("message", "批量插入发生异常");
            }
        }
        return Result.ok(ResultCodeEnum.SUCCESS);
    }


}
