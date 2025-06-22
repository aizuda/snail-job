package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 告警通知接收人
 *
 * @author opensnail
 * @since 2024-04-17
 */
@Getter
@Setter
@TableName("sj_notify_recipient")
public class NotifyRecipient extends CreateUpdateDt {

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 命名空间id
     */
    private String namespaceId;

    /**
     * 接收人名称
     */
    private String recipientName;

    /**
     * 通知类型 1、钉钉 2、邮件 3、企业微信 4 飞书
     */
    private Integer notifyType;

    /**
     * 配置属性
     */
    private String notifyAttribute;

    /**
     * 描述
     */
    private String description;

}
