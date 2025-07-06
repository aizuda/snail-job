package com.aizuda.snailjob.server.common.handler;

import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.client.CommonRpcClient;
import com.aizuda.snailjob.server.common.dto.ConfigSyncTask;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 版本同步
 *
 * @author opensnail
 * @date 2023-06-10
 * @since 1.6.0
 */
@Component
@RequiredArgsConstructor
public class ConfigVersionSyncHandler implements Lifecycle, Runnable {
    private static final LinkedBlockingQueue<ConfigSyncTask> QUEUE = new LinkedBlockingQueue<>(256);
    public Thread THREAD = null;
    private final AccessTemplate accessTemplate;
    private final InstanceManager instanceManager;

    /**
     * 添加任务
     *
     * @param groupName      组
     * @param currentVersion 当前版本号
     * @return false-队列容量已满， true-添加成功
     */
    public boolean addSyncTask(String groupName, String namespaceId, Integer currentVersion) {

        ConfigSyncTask configSyncTask = new ConfigSyncTask();
        configSyncTask.setCurrentVersion(currentVersion);
        configSyncTask.setNamespaceId(namespaceId);
        configSyncTask.setGroupName(groupName);
        return QUEUE.offer(configSyncTask);
    }

    /**
     * 同步版本
     *
     * @param groupName   组
     * @param namespaceId 空间id
     */
    public void syncVersion(String groupName, final String namespaceId) {

        try {
            Set<InstanceLiveInfo> instanceALiveInfoSet = instanceManager.getInstanceALiveInfoSet(namespaceId, groupName);
            // 同步版本到每个客户端节点
            for (final InstanceLiveInfo instanceLiveInfo : instanceALiveInfoSet) {
                ConfigDTO configDTO = accessTemplate.getGroupConfigAccess().getConfigInfo(groupName, namespaceId);
                CommonRpcClient rpcClient = RequestBuilder.<CommonRpcClient, Result>newBuilder()
                        .failover(false)
                        .failRetry(true)
                        .retryTimes(3)
                        .retryInterval(1)
                        .nodeInfo(instanceLiveInfo)
                        .client(CommonRpcClient.class)
                        .build();
                SnailJobLog.LOCAL.info("Synchronization result [{}]", rpcClient.syncConfig(configDTO));
            }
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("version sync error. groupName:[{}]", groupName, e);
        }
    }

    @Override
    public void start() {
        THREAD = new Thread(this, "config-version-sync");
        THREAD.start();
    }

    @Override
    public void close() {
        if (Objects.nonNull(THREAD)) {
            THREAD.interrupt();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ConfigSyncTask task = QUEUE.take();
                // 远程版本号
                Integer remoteVersion = accessTemplate.getGroupConfigAccess().getConfigVersion(task.getGroupName(), task.getNamespaceId());
                if (Objects.isNull(remoteVersion) || !task.getCurrentVersion().equals(remoteVersion)) {
                    syncVersion(task.getGroupName(), task.getNamespaceId());
                }
            } catch (InterruptedException e) {
                SnailJobLog.LOCAL.info("[{}] thread stop.", Thread.currentThread().getName());
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("client refresh expireAt error.", e);
            } finally {
                try {
                    // 防止刷的过快，休眠1s
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
