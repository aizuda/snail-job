package com.aizuda.snailjob.server.common.dto;

import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xiaowoniu
 * @date 2024-01-10 22:56:33
 * @since 3.2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryLogMetaDTO extends LogMetaDTO {

    public RetryLogMetaDTO () {
        setLogType(LogTypeEnum.RETRY);
    }

    /**
     * 组名称
     */
    private String uniqueId;

    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}
