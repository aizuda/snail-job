package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.common.enums.IdGeneratorModeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 组、场景、通知配置类
 *
 * @author: opensnail
 * @date : 2021-11-22 13:45
 * @since 1.0.0
 */
@Data
public class GroupConfigRequestVO {

    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    @NotNull(message = "组状态不能为空")
    private Integer groupStatus;

    /**
     * 令牌
     */
    @NotBlank(message = "令牌不能为空")
    private String token;

    /**
     * 描述
     */
    private String description;

    /**
     * 分区
     */
    @NotNull(message = "分区不能为空")
    private Integer groupPartition;

    /**
     * 唯一id生成模式
     * {@link IdGeneratorModeEnum}
     */
    @NotNull(message = "id生成模式不能为空")
    private Integer idGeneratorMode;

    /**
     * 是否初始化场景
     */
    @NotNull(message = "初始化场不能为空")
    private Integer initScene;

}
