package com.aizuda.snailjob.model.request;

import com.aizuda.snailjob.model.base.StatusUpdateRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.openapi.job.dto
 * @Project：snail-job
 * @Date：2025/7/11 10:22
 * @Filename：JobStatusUpdateRequestDTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StatusUpdateApiRequest extends StatusUpdateRequest {
}
