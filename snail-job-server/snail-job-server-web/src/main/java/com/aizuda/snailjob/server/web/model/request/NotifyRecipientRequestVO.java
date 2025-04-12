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

    @NotNull(message = "id cannot be null", groups = Update.class)
    private Long id;

    /**
     * 接收人名称
     */
    @NotBlank(message = "Recipient name cannot be empty")
    private String recipientName;

    /**
     * 通知类型 1、钉钉 2、邮件 3、企业微信 4 飞书
     */
    @NotNull(message = "Notification type cannot be empty")
    private Integer notifyType;

    /**
     * 配置属性
     */
    @NotBlank(message = "Configuration properties cannot be empty")
    private String notifyAttribute;

    /**
     * 描述
     */
    private String description;
}
