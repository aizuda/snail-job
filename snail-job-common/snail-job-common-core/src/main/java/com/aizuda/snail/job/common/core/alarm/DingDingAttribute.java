package com.aizuda.snail.job.common.core.alarm;

import lombok.Data;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-05-04 16:13
 */
@Data
public class DingDingAttribute {

    private String webhookUrl;

    private List<String> ats;

}
