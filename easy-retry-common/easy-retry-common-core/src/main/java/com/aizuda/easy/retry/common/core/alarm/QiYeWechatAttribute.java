package com.aizuda.easy.retry.common.core.alarm;

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
