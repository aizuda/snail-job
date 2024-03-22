package com.aizuda.easy.retry.server.common;

import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.common.log.enums.LogTypeEnum;
import com.aizuda.easy.retry.server.common.dto.LogMetaDTO;

/**
 * @author: xiaowoniu
 * @date : 2024-03-22
 * @since : 3.2.0
 */
public interface LogStorage {

    LogTypeEnum logType();

    void storage(final LogContentDTO logContentDTO, final LogMetaDTO logMetaDTO);
}
