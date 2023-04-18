package com.aizuda.easy.retry.client.core.client.request;

import com.aizuda.easy.retry.client.core.config.XRetryProperties;
import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2022-03-08
 * @since 2.0
 */
@Data
public class RequestParam {

    private String groupName;

    public String getGroupName() {
        return XRetryProperties.getGroup();
    }
}
