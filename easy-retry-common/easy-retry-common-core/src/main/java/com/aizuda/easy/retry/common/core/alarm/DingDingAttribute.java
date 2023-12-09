package com.aizuda.easy.retry.common.core.alarm;

import lombok.Data;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-04 16:13
 */
@Data
public class DingDingAttribute {

    private String webhookUrl;

    private List<String> ats;

}
