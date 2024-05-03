package com.aizuda.snailjob.common.core.alarm.attribute;

import lombok.Data;

import java.util.List;

/**
 * 企业微信
 *
 * @author lizhongyuan
 */
@Data
public class QiYeWechatAttribute {

    private String webhookUrl;

    private List<String> ats;

}
