package com.x.retry.server.model.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * 重试上报DTO
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-25 14:11
 */
@Data
public class RetryTaskDTO implements Serializable {

    /**
     * 加密的groupId
     */
    @NotBlank(message = "shardingGroupId 不能为空")
    @Length(max = 16, message = "组id最长为16")
    private String groupName;

    /**
     * 加密的sceneId
     */
    @NotBlank(message = "sceneId 不能为空")
    @Length(max = 16, message = "场景id最长为16")
    private String sceneName;

    /**
     * 业务唯一id
     */
    @NotBlank(message = "bizId 不能为空")
    @Length(max = 64, message = "业务唯一id最长为64")
    private String bizId;

    /**
     * 执行器名称
     */
    @NotBlank(message = "executorName 不能为空")
    @Length(max = 512, message = "业务唯一id最长为512")
    private String executorName;

    /**
     * 业务唯一编号
     */
    @Length(max = 64, message = "业务唯一编号最长为64")
    private String bizNo;

    /**
     * 客户端上报参数
     */
    @NotBlank(message = "argsStr 不能为空")
    private String argsStr;

    /**
     * 额外扩展参数
     */
    private String extAttrs;

}
