package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.annotation.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author opensnail
 * @date 2024-04-17 22:03:33
 * @since sj_1.0.0
 */
@Data
public class NotifyRecipientRequestVO {

    @NotNull(message = "id不能为空", groups = Update.class)
    private Long id;

    /**
     * 接收人名称
     */
    @NotBlank(message = "接收人名称不能为空")
    private String recipientName;

    /**
     * 通知类型 1、钉钉 2、邮件 3、企业微信 4 飞书
     */
    @NotNull(message = "通知类型不能为空")
    private Integer notifyType;

    /**
     * 配置属性
     */
    @NotBlank(message = "配置属性不能为空")
    private String notifyAttribute;

    /**
     * 描述
     */
    private String description;
}
