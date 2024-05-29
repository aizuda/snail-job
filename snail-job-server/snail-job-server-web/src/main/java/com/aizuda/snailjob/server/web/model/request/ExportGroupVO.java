package com.aizuda.snailjob.server.web.model.request;

import lombok.Data;

import java.util.Set;

/**
 * @author: opensnail
 * @date : 2024-05-29
 * @since : sj_1.0.0
 */
@Data
public class ExportGroupVO {

    private String groupName;

    private Integer groupStatus;

    private Set<Long> groupIds;

}
