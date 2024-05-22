package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 告警通知接收人
 * </p>
 *
 * @author opensnail
 * @since 2024-04-17
 */
@Getter
@Setter
@TableName("sj_notify_recipient")
public class NotifyRecipient implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
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

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;
}
