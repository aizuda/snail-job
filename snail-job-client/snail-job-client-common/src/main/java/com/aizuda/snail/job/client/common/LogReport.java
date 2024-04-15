package com.aizuda.snail.job.client.common;

import com.aizuda.snail.job.common.log.dto.LogContentDTO;

/**
 * @author xiaowoniu
 * @date 2024-03-20 22:56:53
 * @since 3.2.0
 */
public interface LogReport {

    boolean supports();

    void report(LogContentDTO logContentDTO);
}
