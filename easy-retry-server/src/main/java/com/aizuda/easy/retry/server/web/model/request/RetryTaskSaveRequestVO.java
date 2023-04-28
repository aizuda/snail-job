package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 重试数据模型
 *
 * @author www.byteblogs.com
 * @date 2023-09-29
 * @since 2.0
 */
@Data
public class RetryTaskSaveRequestVO {

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
     * 业务id(同一个场景下正在重试中的bizId不能重复)
     */
    @NotBlank(message = "业务id不能为空")
    private String bizId;

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
