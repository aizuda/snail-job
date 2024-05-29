package com.aizuda.snailjob.server.web.model.request;

import lombok.Data;

import java.util.Set;

/**
 * @author: opensnail
 * @date : 2024-05-29
 * @since : sj_1.0.0
 */
@Data
public class ExportSceneVO {

    private String groupName;

    private Integer sceneStatus;

    private String sceneName;

    private Set<Long> sceneIds;

}
