package com.gyr.milvusactual.entity;

import com.sun.xml.internal.bind.v2.model.core.ReferencePropertyInfo;
import lombok.Data;

/**
 * @author guoyr
 * @description
 * @date 2022/2/18 15:32
 */
@Data
public class SearchResultVo {

    private Long faceId;

    private Long gridId;

    private Long captureTime;

    private Float score;
}
