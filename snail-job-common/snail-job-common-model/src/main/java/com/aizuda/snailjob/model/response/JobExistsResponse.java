package com.aizuda.snailjob.model.response;

import com.aizuda.snailjob.common.core.enums.StatusEnum;
import lombok.Data;

/**
 * 基于 bizId 查询任务是否存在的结果响应
 *
 * @author opensnail
 * @since 1.10.0
 */
@Data
public class JobExistsResponse {

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 业务ID
     */
    private String bizId;

    /**
     * 状态 0、关闭、1、开启
     * {@link StatusEnum}
     */
    private Integer jobStatus;

}