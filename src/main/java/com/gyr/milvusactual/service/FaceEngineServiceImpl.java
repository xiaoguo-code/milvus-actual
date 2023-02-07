package com.gyr.milvusactual.service;


import com.arcsoft.face.FaceInfo;
import com.gyr.milvusactual.common.face.FaceEngineUtil;
import com.gyr.milvusactual.common.result.Result;
import com.gyr.milvusactual.common.result.ResultCodeEnum;
import com.gyr.milvusactual.common.util.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class FaceEngineServiceImpl implements FaceEngineService {


    @Autowired
    private FaceEngineUtil faceEngineUtil;

    @Override
    public byte[] faceFindCropFeature(File file) {
        try {

            String name = file.getName();
            List<FaceInfo> faceInfos = null;
            faceInfos = faceEngineUtil.faceFind(new FileInputStream(file));

            if (faceInfos.size() == 0) {
                log.warn("file:{},人脸信息不存在", name);
                return null;
            } else if (faceInfos.size() > 1) {
                log.warn("file:{},存在多个人脸", name);
                return null;
            } else {
                String newImageBase64 = faceEngineUtil.faceCrop(new FileInputStream(file), faceInfos.get(0).getRect());
                if (StringUtils.isBlank(newImageBase64)) {
                    log.warn("file:{},人脸信息不存在", name);
                    return null;
                }
                // 人脸信息正确 提取特征向量
                // 进行特征值提取
                byte[] feature = faceEngineUtil.faceFeature(new FileInputStream(file), faceInfos.get(0));
                if (feature.length == 0) {
                    log.warn("file:{},特征值长度为0", name);
                    return null;
                }
                return feature;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
