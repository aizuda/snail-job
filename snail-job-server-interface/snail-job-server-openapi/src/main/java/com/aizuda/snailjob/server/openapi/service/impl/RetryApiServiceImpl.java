package com.aizuda.snailjob.server.openapi.service.impl;

import com.aizuda.snailjob.server.openapi.service.RetryApiService;
import com.aizuda.snailjob.server.openapi.util.OpenApiSessionUtils;
import com.aizuda.snailjob.server.service.service.impl.AbstractRetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-25
 */
@Service
@RequiredArgsConstructor
public class RetryApiServiceImpl extends AbstractRetryService implements RetryApiService {
    @Override
    protected String getNamespaceId() {
        return OpenApiSessionUtils.getNamespaceId();
    }
}
