package com.aizuda.easy.retry.client.common.rpc.supports.scan;

import com.aizuda.easy.retry.client.common.Lifecycle;
import com.aizuda.easy.retry.client.common.cache.EndPointInfoCache;
import com.aizuda.easy.retry.client.common.exception.EasyRetryClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author opensnail
 * @date 2024-04-11 22:57:03
 * @since 3.3.0
 */
@Component
@RequiredArgsConstructor
public class SnailEndPointRegistrar implements Lifecycle {
    private final SnailEndPointScanner snailEndPointScanner;

    @Override
    public void start() {
        List<EndPointInfo> endPointInfos = snailEndPointScanner.doScan();
        for (EndPointInfo endPointInfo : endPointInfos) {
            if (EndPointInfoCache.isExisted(endPointInfo.getPath(), endPointInfo.getRequestMethod())) {
                throw new EasyRetryClientException("Duplicate endpoint path: {}" , endPointInfo.getPath());
            }

            EndPointInfoCache.put(endPointInfo);
        }

    }

    @Override
    public void close() {

    }
}
