package com.aizuda.snailjob.server.common;

import com.aizuda.snailjob.common.log.dto.LogContentDTO;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.aizuda.snailjob.server.common.dto.LogMetaDTO;
import com.aizuda.snailjob.server.common.dto.LogMetaDTO;

/**
 * @author: xiaowoniu
 * @date : 2024-03-22
 * @since : 3.2.0
 */
public interface LogStorage {

    LogTypeEnum logType();

    void storage(final LogContentDTO logContentDTO, final LogMetaDTO logMetaDTO);
}
