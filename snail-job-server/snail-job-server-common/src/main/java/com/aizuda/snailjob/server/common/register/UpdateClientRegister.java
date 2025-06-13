package com.aizuda.snailjob.server.common.register;

import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.common.client.CommonRpcClient;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public void updateClientInfo(ServerNode serverNode){
        Set<InstanceLiveInfo> instanceALiveInfoSet = instanceManager.getInstanceALiveInfoSet(ServerRegister.NAMESPACE_ID, ServerRegister.GROUP_NAME);
        instanceALiveInfoSet = instanceALiveInfoSet.stream().filter(info -> !info.getNodeInfo().getHostId().equals(ServerRegister.CURRENT_CID)).collect(Collectors.toSet());

        if (null != serverNode){
            // 更新本地标签
            instanceManager.updateInstanceLabels(serverNode);
        }

        if (!instanceALiveInfoSet.isEmpty()){
            instanceALiveInfoSet.forEach(info ->{
                threadPoolExecutor.execute(() -> {
                    CommonRpcClient serverRpcClient = buildRpcClient(info);
                    serverRpcClient.updateClientInfo(serverNode);
                });
            });
        }
    }

    private CommonRpcClient buildRpcClient(InstanceLiveInfo info) {

        int maxRetryTimes = 3;
        return RequestBuilder.<CommonRpcClient, Result>newBuilder().nodeInfo(info).failRetry(true).retryTimes(maxRetryTimes).client(CommonRpcClient.class).build();
    }
}
