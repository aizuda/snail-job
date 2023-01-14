package com.x.retry.common.core.enums;

import lombok.Getter;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-26 18:01
 */
@Getter
public enum NotifyTypeEnum {

    /**
     * 钉钉通知
     */
    DING_DING(1),

    /**
     * 邮箱通知
     */
    EMAIL(2),

    /**
     * 企业微信
     */
    QI_YE_WECHAT(3),
    ;

    private final Integer type;

    NotifyTypeEnum(int type) {
        this.type = type;
    }


}
