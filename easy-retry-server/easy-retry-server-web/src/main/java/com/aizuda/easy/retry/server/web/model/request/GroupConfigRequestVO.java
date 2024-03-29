package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.server.common.enums.IdGeneratorModeEnum;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 组、场景、通知配置类
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-22 13:45
 * @since 1.0.0
 */
@Data
public class GroupConfigRequestVO {

    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
    private String groupName;

    @NotNull(message = "组状态不能为空")
    private Integer groupStatus;

    /**
     * 令牌
     */
    private String token;

    /**
     * 描述
     */
    private String description;

    /**
     * 分区
     */
    private Integer groupPartition;

    /**
     * 唯一id生成模式
     * {@link IdGeneratorModeEnum}
     */
    private Integer idGeneratorMode;

    /**
     * 是否初始化场景
     */
    private Integer initScene;

}
