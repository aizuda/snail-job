package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-02-25 13:42
 */
@Data
public class GroupConfigResponseVO {

    private Long id;

    private String groupName;

    private String namespaceId;

    private String namespaceName;

    private Integer groupStatus;

    private Integer groupPartition;

    private Integer routeKey;

    private Integer version;

    private String description;

    private Integer idGeneratorMode;

    private String idGeneratorModeName;

    private Integer initScene;

    private List<String> onlinePodList;

    private String token;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;
}
