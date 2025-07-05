package com.aizuda.snailjob.server.web.model.request;

import lombok.Data;

import java.util.Set;

/**
 * @author opensnail
 * @date 2024-05-30 21:49:19
 * @since sj_1.0.0
 */
@Data
public class ExportJobVO {

    private Set<Long> jobIds;
    private String groupName;
    private String jobName;
    private Integer jobStatus;
}
