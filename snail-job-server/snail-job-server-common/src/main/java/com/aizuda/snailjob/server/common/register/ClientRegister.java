package com.aizuda.snailjob.server.common.register;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.NodeTypeEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.cache.CacheConsumerGroup;
import com.aizuda.snailjob.server.common.client.CommonRpcClient;
import com.aizuda.snailjob.server.common.convert.RegisterNodeInfoConverter;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.PullRemoteNodeClientRegisterInfoDTO;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 客户端注册
 *
 * @author opensnail
 * @date 2023-06-07
 * @since 1.6.0
 */
public class ClientRegister extends AbstractRegister {
    public static final String BEAN_NAME = "clientRegister";
    public static final int DELAY_TIME = 30;
    protected static final LinkedBlockingDeque<ServerNode> QUEUE = new LinkedBlockingDeque<>(1000);
    @Setter
    private RefreshNodeSchedule refreshNodeSchedule;

    @Override
    public boolean supports(int type) {
        return getNodeType().equals(type);
    }

    @Override
    protected void beforeProcessor(RegisterContext context) {
    }

    @Override
    protected LocalDateTime getExpireAt() {
        return LocalDateTime.now().plusSeconds(DELAY_TIME);
    }

    @Override
    protected boolean doRegister(RegisterContext context, ServerNode serverNode) {
        if (HTTP_PATH.BEAT.equals(context.getUri())) {
            return QUEUE.offerFirst(serverNode);
        }

        return QUEUE.offerLast(serverNode);
    }

    @Override
    protected void afterProcessor(final ServerNode serverNode) {
    }

    @Override
    protected Integer getNodeType() {
        return NodeTypeEnum.CLIENT.getType();
    }

    @Override
    public void start() {
        refreshNodeSchedule.startScheduler();
    }

    @Override
    public void close() {
    }

    public static List<ServerNode> getExpireNodes() {

        ServerNode serverNode = QUEUE.poll();
        if (Objects.nonNull(serverNode)) {
            List<ServerNode> lists = Lists.newArrayList(serverNode);
            QUEUE.drainTo(lists, 256);
            return lists;
        }

        return null;
    }

    public List<ServerNode> refreshLocalCache() {
        // 获取当前所有需要续签的node
        List<ServerNode> expireNodes = ClientRegister.getExpireNodes();
        if (Objects.nonNull(expireNodes)) {
            // 进行本地续签
            for (final ServerNode serverNode : expireNodes) {
                serverNode.setExpireAt(LocalDateTime.now().plusSeconds(DELAY_TIME));
                // 刷新全量本地缓存
                instanceManager.registerOrUpdate(RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(serverNode));
                // 刷新过期时间
                CacheConsumerGroup.addOrUpdate(serverNode.getGroupName(), serverNode.getNamespaceId());
            }
        }
        return expireNodes;
    }

    public RefreshNodeSchedule newRefreshNodeSchedule(ServerNodeMapper serverNodeMapper, InstanceManager instanceManager) {
        return new RefreshNodeSchedule(serverNodeMapper, instanceManager);
    }

    @RequiredArgsConstructor
    public class RefreshNodeSchedule extends AbstractSchedule {
        private ThreadPoolExecutor refreshNodePool;
        private final ServerNodeMapper serverNodeMapper;
        private final InstanceManager instanceManager;

        @Override
        protected void doExecute() {
            try {
                // 获取在线的客户端节点并且排除当前节点
                Set<InstanceLiveInfo> instanceALiveInfoSet = instanceManager.getInstanceALiveInfoSet(ServerRegister.NAMESPACE_ID, ServerRegister.GROUP_NAME);
                instanceALiveInfoSet = instanceALiveInfoSet.stream().filter(info -> !info.getNodeInfo().getHostId().equals(ServerRegister.CURRENT_CID)).collect(Collectors.toSet());

                List<ServerNode> waitRefreshDBClientNodes = new ArrayList<>();

                // 刷新本地缓存
                List<ServerNode> refreshCache = refreshLocalCache();
                if (CollUtil.isNotEmpty(refreshCache)) {
                    // 完成本节点的刷新
                    waitRefreshDBClientNodes.addAll(refreshCache);
                }

                if (!instanceALiveInfoSet.isEmpty()) {
                    // 并行获取所有服务端需要注册的列表
                    // 获取列表 并完成注册/本地完成续签
                    List<ServerNode> allClientList = pullRemoteNodeClientRegisterInfo(instanceALiveInfoSet);
                    if (CollUtil.isNotEmpty(allClientList)) {
                        waitRefreshDBClientNodes.addAll(allClientList);
                    }
                }

                if (CollUtil.isEmpty(waitRefreshDBClientNodes)) {
                    SnailJobLog.LOCAL.debug("clientNodes is empty");
                    return;
                }

                SnailJobLog.LOCAL.debug("start refresh client nodes：{}", waitRefreshDBClientNodes);

                // 刷新DB
                refreshExpireAt(waitRefreshDBClientNodes);

            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Refresh failed", e);
            }
        }

        private List<ServerNode> pullRemoteNodeClientRegisterInfo(Set<InstanceLiveInfo> infos) {
            if (CollUtil.isEmpty(infos)) {
                return Lists.newArrayList();
            }

            int size = infos.size();
            // 存储处理结果
            List<Future<String>> futures = new ArrayList<>(size);
            for (InstanceLiveInfo liveInfo : infos) {
                Future<String> future = refreshNodePool.submit(() -> {
                    try {
                        CommonRpcClient serverRpcClient = buildRpcClient(liveInfo);
                        Result<String> regNodesAndFlush = serverRpcClient.pullRemoteNodeClientRegisterInfo(new PullRemoteNodeClientRegisterInfoDTO());
                        return regNodesAndFlush.getData();
                    } catch (Exception e) {
                        return StrUtil.EMPTY;
                    }
                });

                futures.add(future);
            }

            return futures.stream().map(future -> {
                try {
                    // 后面可以考虑配置
                    String jsonString = future.get(1, TimeUnit.SECONDS);
                    if (Objects.nonNull(jsonString)) {
                        return JsonUtil.parseList(jsonString, ServerNode.class);
                    }
                    return new ArrayList<ServerNode>();
                } catch (Exception e) {
                    return new ArrayList<ServerNode>();
                }
            }).filter(Objects::nonNull).flatMap(List::stream).distinct().toList();

        }

        private CommonRpcClient buildRpcClient(InstanceLiveInfo info) {

            int maxRetryTimes = 3;
            return RequestBuilder.<CommonRpcClient, Result>newBuilder().nodeInfo(info).failRetry(true).retryTimes(maxRetryTimes).client(CommonRpcClient.class).build();
        }

        @Override
        public String lockName() {
            return "registerNode";
        }

        @Override
        public String lockAtMost() {
            return "PT10S";
        }

        @Override
        public String lockAtLeast() {
            return "PT5S";
        }

        public void startScheduler() {
            // 后面可以考虑配置
            refreshNodePool = new ThreadPoolExecutor(4, 8, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
            refreshNodePool.allowCoreThreadTimeOut(true);
            taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT5S"));
        }

    }
}
