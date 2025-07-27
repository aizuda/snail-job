package com.aizuda.snailjob.server.openapi.api;

import com.aizuda.snailjob.server.openapi.dto.RetryResponseApiDTO;
import com.aizuda.snailjob.model.request.StatusUpdateApiRequest;
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
    private final RetryService retryApiService;

    @GetMapping(OPENAPI_QUERY_RETRY)
    public RetryResponseApiDTO getRetryTaskById(@RequestParam("id") Long id) {
        return retryApiService.getRetryById(id, RetryResponseApiDTO.class);
    }

    @PutMapping(OPENAPI_UPDATE_RETRY_STATUS_V2)
    public boolean updateRetryTaskStatus(@RequestBody @Validated StatusUpdateApiRequest requestDTO) {
        return retryApiService.updateRetryStatus(requestDTO);
    }

    @PostMapping(OPENAPI_TRIGGER_RETRY_V2)
    public boolean triggerRetry(@RequestBody @Validated TriggerRetryDTO requestDTO) {
        return retryApiService.triggerRetry(requestDTO);
    }
}
