package com.aizuda.snailjob.server.common.register;

import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.client.CommonRpcClient;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.UpdateClientInfoDTO;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 客户端刷新注册注册
 *
 * @author opensnail
 * @since 1.6.0
 */
@RequiredArgsConstructor
@Component
public class UpdateClientRegister {
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
    private final InstanceManager instanceManager;

    public boolean updateClientInfo(UpdateClientInfoDTO clientInfoDTO) {
        if (Objects.isNull(clientInfoDTO)){
            return false;
        }
        Set<InstanceLiveInfo> instanceALiveInfoSet = instanceManager.getInstanceALiveInfoSet(ServerRegister.NAMESPACE_ID, ServerRegister.GROUP_NAME);
        instanceALiveInfoSet = instanceALiveInfoSet.stream().filter(info -> !info.getNodeInfo().getHostId().equals(ServerRegister.CURRENT_CID)).collect(Collectors.toSet());

        boolean result = true;
        if (!instanceALiveInfoSet.isEmpty()) {
            List<Boolean> results = new ArrayList<>();
            instanceALiveInfoSet.stream().map(info -> threadPoolExecutor.submit((Callable<Result>) () -> {
                CommonRpcClient serverRpcClient = buildRpcClient(info);
                return serverRpcClient.updateClientInfo(clientInfoDTO);
            })).forEach(future -> {
                try {
                    Result<Boolean> futureResult = future.get(1, TimeUnit.SECONDS);
                    if (StatusEnum.NO.getStatus() == futureResult.getStatus()
                            || (Objects.nonNull(futureResult.getData()) && !futureResult.getData())) {
                        SnailJobLog.LOCAL.error("update client info error. msg:[{}]", futureResult.getMessage());
                        results.add(false);
                    } else {
                        results.add(true);
                    }
                } catch (Throwable e) {
                    SnailJobLog.LOCAL.error("update client info error", e);
                    results.add(false);
                }
            });

            result = results.stream().allMatch(Boolean::booleanValue);
        }

        return result && instanceManager.updateInstanceLabels(clientInfoDTO);
    }

    private CommonRpcClient buildRpcClient(InstanceLiveInfo info) {

        int maxRetryTimes = 3;
        return RequestBuilder.<CommonRpcClient, Result>newBuilder().nodeInfo(info).failRetry(true).retryTimes(maxRetryTimes).client(CommonRpcClient.class).build();
    }
}
