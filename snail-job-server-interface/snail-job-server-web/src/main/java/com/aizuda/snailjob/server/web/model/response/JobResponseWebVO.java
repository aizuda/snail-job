package com.aizuda.snailjob.server.web.model.response;

import com.aizuda.snailjob.model.response.base.JobResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author opensnail
 * @date 2023-10-11 22:30:00
 * @since 2.4.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobResponseWebVO extends JobResponse {

    /**
     * 负责人名称
     */
    private String ownerName;

    /**
     * 负责人id
     */
    private Long ownerId;


}
