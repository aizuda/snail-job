package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预警类型
 *
 * @author: opensnail
 * @date : 2021-11-25 09:19
 */
@AllArgsConstructor
@Getter
public enum AlarmTypeEnum {

    /**
     * 钉钉通知
     */
    DING_DING(1),

    /**
     * 邮件通知
     */
    EMAIL(2),

    /**
     * 企业微信通知
     */
    WE_COM(3),

    /**
     * 飞书
     */
    LARK(4),

    /**
     * Webhook
     */
    WEBHOOK(5),
    ;

    private final int value;

}
