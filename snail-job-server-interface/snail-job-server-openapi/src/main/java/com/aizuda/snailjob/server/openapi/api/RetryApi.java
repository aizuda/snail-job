package com.aizuda.snailjob.server.openapi.api;

import com.aizuda.snailjob.server.openapi.dto.RetryResponseDTO;
import com.aizuda.snailjob.server.openapi.dto.StatusUpdateRequestDTO;
import com.aizuda.snailjob.server.service.dto.TriggerRetryDTO;
import com.aizuda.snailjob.server.service.service.RetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.*;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-25
 */
@RestController
@RequiredArgsConstructor
public class RetryApi {
    private final RetryService retryService;

    @GetMapping(OPENAPI_QUERY_RETRY)
    public RetryResponseDTO getRetryTaskById(@RequestParam("id") Long id) {
        return retryService.getRetryById(id, RetryResponseDTO.class);
    }

    @PutMapping(OPENAPI_UPDATE_RETRY_STATUS_V2)
    public boolean updateRetryTaskStatus(@RequestBody @Validated StatusUpdateRequestDTO requestDTO) {
        return retryService.updateRetryStatus(requestDTO);
    }

    @PutMapping(OPENAPI_TRIGGER_RETRY_V2)
    public boolean triggerRetry(@RequestBody @Validated TriggerRetryDTO requestDTO) {
        return retryService.triggerRetry(requestDTO);
    }
}
