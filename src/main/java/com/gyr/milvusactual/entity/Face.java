package com.gyr.milvusactual.entity;

/**
 * @author guoyr
 * @description
 * @date 2022/2/16 10:28
 */
public class Face {

    private Long faceId;

    private Double qualityScore;
    private Double lon;
    private Double lat;

    private Integer captureTime;



    private String area;


    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Long getFaceId() {
        return faceId;
    }

    public void setFaceId(Long faceId) {
        this.faceId = faceId;
    }

    public Double getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Double qualityScore) {
        this.qualityScore = qualityScore;
    }

    public Integer getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Integer captureTime) {
        this.captureTime = captureTime;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
