package com.aizuda.easy.retry.server.retry.task.support.handler;

import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.core.util.NetUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.retry.task.dto.ConfigSyncTask;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 版本同步
 *
 * @author www.byteblogs.com
 * @date 2023-06-10
 * @since 1.6.0
 */
@Component
@Slf4j
public class ConfigVersionSyncHandler implements Lifecycle, Runnable {

    private static final LinkedBlockingQueue<ConfigSyncTask> QUEUE = new LinkedBlockingQueue<>(256);
    public Thread THREAD = null;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    protected AccessTemplate accessTemplate;
    private static final String SYNC_VERSION_V1 =  "/retry/sync/version/v1";

    /**
     * 添加任务
     *
     * @param groupName 组
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
     * @param groupName
     * @param namespaceId
     */
    public void syncVersion(String groupName, final String namespaceId) {

        try {
            Set<RegisterNodeInfo> serverNodeSet = CacheRegisterTable.getServerNodeSet(groupName, namespaceId);
            // 同步版本到每个客户端节点
            for (final RegisterNodeInfo registerNodeInfo : serverNodeSet) {
                ConfigDTO configDTO = accessTemplate.getGroupConfigAccess().getConfigInfo(groupName, namespaceId);

                String url = NetUtil.getUrl(registerNodeInfo.getHostIp(), registerNodeInfo.getHostPort(),
                        registerNodeInfo.getContextPath());
                Result result = restTemplate.postForObject(url.concat(SYNC_VERSION_V1), configDTO, Result.class);
               EasyRetryLog.LOCAL.info("同步结果 [{}]", result);
            }
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("version sync error. groupName:[{}]", groupName, e);
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
               EasyRetryLog.LOCAL.info("[{}] thread stop.", Thread.currentThread().getName());
            } catch (Exception e) {
                EasyRetryLog.LOCAL.error("client refresh expireAt error.", e);
            } finally {
                try {
                    // 防止刷的过快，休眠1s
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
