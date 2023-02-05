package com.gyr.milvusactual.entity;

import java.util.List;

/**
 * @author guoyr
 * @description
 * @date 2022/2/16 10:28
 */
public class FaceTest {

    private Long faceId;

    private Long gridId;

    private Long captureTime;

    private List<Float> feature;

    public Long getFaceId() {
        return faceId;
    }

    public void setFaceId(Long faceId) {
        this.faceId = faceId;
    }

    public Long getGridId() {
        return gridId;
    }

    public void setGridId(Long gridId) {
        this.gridId = gridId;
    }

    public Long getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Long captureTime) {
        this.captureTime = captureTime;
    }

    public List<Float> getFeature() {
        return feature;
    }

    public void setFeature(List<Float> feature) {
        this.feature = feature;
    }
}
