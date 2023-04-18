package com.aizuda.easy.retry.common.core.enums;

/**
 * 预警类型
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-25 09:19
 */
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
     * 企业通知
     */
    QI_YE_WECHAT(3),
    ;

    private final int value;

    AlarmTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
