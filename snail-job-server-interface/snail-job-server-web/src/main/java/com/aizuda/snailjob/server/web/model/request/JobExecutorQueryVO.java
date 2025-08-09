package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.service.dto.base.BaseQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author opensnail
 * @date 2023-10-11 22:28:07
 * @since 2.4.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobExecutorQueryVO extends BaseQueryDTO {
    private String groupName;
    private String executorInfo;
    /**
     * 1:java; 2:python; 3:go;
     */
    private String executorType;
}
