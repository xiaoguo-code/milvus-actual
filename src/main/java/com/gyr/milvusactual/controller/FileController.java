package com.gyr.milvusactual.controller;

import com.arcsoft.face.FaceInfo;
import com.gyr.milvusactual.common.face.FaceEngineUtil;
import com.gyr.milvusactual.common.result.Result;
import com.gyr.milvusactual.common.result.ResultCodeEnum;
import com.gyr.milvusactual.common.util.Base64Utils;
import com.gyr.milvusactual.common.util.SnowflakeIdWorker;
import com.gyr.milvusactual.common.util.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/file")
//@Tag(name = "文件上传接口")
@Slf4j
@CrossOrigin
public class FileController {
    /**
     * 文件上传后的存储位置
     */
    @Value("${uploadFile.templocation:/Users/admin/Documents/idea/code/milvus-actual/img}")
    String tempFilepath;


    @Autowired
    FaceEngineUtil faceEngineUtil;

//    @Autowired
//    faceList faceList;

//    @Autowired
//    RedisService redisService;


    @PostMapping("/getImageUrl")
//    @Operation(summary = "获取照片URL")
    public Result getImageUrl(@RequestParam("file") MultipartFile file) {
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

    @PostMapping("/getListImageUrl")
//    @Operation(summary = "批量上传照片")
    public Result getListImageUrl(@RequestPart("file") MultipartFile file) {
        if (!file.isEmpty()) {
            String uploadPath = tempFilepath + "/" + UUID.randomUUID().toString();
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
                try {
                    ZipUtils.zipUncompress(uploadPath + File.separator + filename, uploadPath);  // windows 用// linux 用 /
                    File photoList = new File(uploadPath + File.separator + uploadFileName);
                    System.out.println(uploadPath + File.separator + uploadFileName);
                    File[] listFiles = photoList.listFiles();
                    for (int i = 0; i < listFiles.length; i++) {
                        File file1 = listFiles[0];
                        log.info(file1.getName());
                    }
//                    List<upResult> upResults = faceList.faceListCheck(listFiles,sessionId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.error();
                }
                return Result.ok().data("path", uploadPath + File.separator + filename);//上传成功，返回保存的文件地址
            } catch (IOException e) {
                e.printStackTrace();
                log.error("上传失败");
                return Result.error();
            }
        } else {
            log.error("文件为空");
            return Result.error();
        }

    }


}