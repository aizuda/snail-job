package com.aizuda.easy.retry.server.support.handler;

import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.aizuda.easy.retry.server.support.cache.CacheRegisterTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author www.byteblogs.com
 * @date 2022-03-09
 * @since 1.6.0
 */
@Component
@Slf4j
public class ConfigVersionSyncHandler implements Lifecycle, Runnable {

    private static final LinkedBlockingQueue<Pair<String/*groupName*/, Integer/*版本号*/>> QUEUE = new LinkedBlockingQueue<>(
        256);

    public static final String URL = "http://{0}:{1}/{2}/retry/sync/version/v1";
    public static Thread THREAD = null;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    /**
     * 添加任务
     *
     * @param groupName 组
     * @param currentVersion 当前版本号
     * @return false-队列容量已满， true-添加成功
     */
    public boolean addSyncTask(String groupName, Integer currentVersion) {
        return QUEUE.offer(Pair.of(groupName, currentVersion));
    }

    /**
     * 同步版本
     * @param groupName
     */
    public void syncVersion(String groupName) {

        try {
            Set<RegisterNodeInfo> serverNodeSet = CacheRegisterTable.getServerNodeSet(groupName);
            // 同步版本到每个客户端节点
            for (final RegisterNodeInfo registerNodeInfo : serverNodeSet) {
                ConfigDTO configDTO = configAccess.getConfigInfo(groupName);
                String format = MessageFormat.format(URL, registerNodeInfo.getHostIp(), registerNodeInfo.getHostPort().toString(),
                    registerNodeInfo.getContextPath());
                Result result = restTemplate.postForObject(format, configDTO, Result.class);
                LogUtils.info(log, "同步结果 [{}]", result);
            }
        } catch (Exception e) {
            LogUtils.error(log, "version sync error. groupName:[{}]", groupName, e);
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
                Pair<String, Integer> pair = QUEUE.take();
                // 远程版本号
                Integer remoteVersion = configAccess.getConfigVersion(pair.getKey());
                if (Objects.isNull(remoteVersion) || !pair.getValue().equals(remoteVersion)) {
                    syncVersion(pair.getKey());
                }
            } catch (InterruptedException e) {
                LogUtils.error(log, "[{}] thread interrupt.", Thread.currentThread().getName());
            } catch (Exception e) {
                LogUtils.error(log, "client refresh expireAt error.", e);
            } finally {
                try {
                    // 防止刷的过快，休眠1s
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
