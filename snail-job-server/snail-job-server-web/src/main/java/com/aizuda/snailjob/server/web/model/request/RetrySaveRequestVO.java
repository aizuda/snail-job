package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重试数据模型
 *
 * @author opensnail
 * @date 2023-09-29
 * @since 2.0
 */
@Data
public class RetrySaveRequestVO {

    /**
     * 组名称
     */
    @NotBlank(message = "组名称不能为空")
    private String groupName;

    /**
     * 场景名称
     */
    @NotBlank(message = "场景名称不能为空")
    private String sceneName;

    /**
     * 幂等id(同一个场景下正在重试中的idempotentId不能重复)
     */
    @NotBlank(message = "幂等id不能为空")
    private String idempotentId;

    /**
     * 业务编号
     */
    private String bizNo;

    /**
     * 重试参数
     */
    private String argsStr;

    /**
     * 扩展重试参数
     */
    private String extAttrs;

    /**
     * 执行器名称(全类路径)
     */
    @NotBlank(message = "执行器名称不能为空")
    private String executorName;

    /**
     * 重试状态 {@link RetryStatusEnum}
     */
    private Integer retryStatus;

}
