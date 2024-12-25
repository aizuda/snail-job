package com.aizuda.snailjob.server.common.register;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.NodeTypeEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.cache.CacheConsumerGroup;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.client.CommonRpcClient;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 客户端注册
 *
 * @author opensnail
 * @date 2023-06-07
 * @since 1.6.0
 */
@Component(ClientRegister.BEAN_NAME)
@Slf4j
public class ClientRegister extends AbstractRegister {
    public static final String BEAN_NAME = "clientRegister";
    public static final int DELAY_TIME = 30;
    protected static final LinkedBlockingDeque<ServerNode> QUEUE = new LinkedBlockingDeque<>(1000);
    @Autowired
    @Lazy
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
        try {
            ServerNode serverNode = QUEUE.poll(5L, TimeUnit.SECONDS);
            if (Objects.nonNull(serverNode)) {
                List<ServerNode> lists = Lists.newArrayList(serverNode);
                QUEUE.drainTo(lists, 256);
                return lists;
            }
        } catch (InterruptedException e) {
            SnailJobLog.LOCAL.error("client get expireNodes error.");
        }
        return null;
    }

    public static List<ServerNode> refreshLocalCache() {
        // 获取当前所有需要续签的node
        List<ServerNode> expireNodes = ClientRegister.getExpireNodes();
        if (Objects.nonNull(expireNodes)) {
            // 进行本地续签
            for (final ServerNode serverNode : expireNodes) {
                serverNode.setExpireAt(LocalDateTime.now().plusSeconds(DELAY_TIME));
                // 刷新全量本地缓存
                CacheRegisterTable.addOrUpdate(serverNode);
                // 刷新过期时间
                CacheConsumerGroup.addOrUpdate(serverNode.getGroupName(), serverNode.getNamespaceId());
            }
        }
        return expireNodes;
    }

    @Component
    public class RefreshNodeSchedule extends AbstractSchedule {
        private final ExecutorService executorService = Executors.newFixedThreadPool(5);

        @Override
        protected void doExecute() {
            try {
                // 获取在线的客户端节点并且排除当前节点
                LambdaQueryWrapper<ServerNode> wrapper = new LambdaQueryWrapper<ServerNode>()
                        .eq(ServerNode::getNodeType, NodeTypeEnum.SERVER.getType());
                List<ServerNode> serverNodes = serverNodeMapper.selectList(wrapper);

                serverNodes = StreamUtils.filter(serverNodes, serverNode -> !serverNode.getHostId().equals(ServerRegister.CURRENT_CID));

                List<ServerNode> waitRefreshDBClientNodes = new ArrayList<>();

                // 刷新本地缓存
                List<ServerNode> refreshCache = refreshLocalCache();
                if (CollUtil.isNotEmpty(refreshCache)) {
                    // 完成本节点的刷新
                    waitRefreshDBClientNodes.addAll(refreshCache);
                }

                if (!serverNodes.isEmpty()) {
                    // 并行获取所有服务端需要注册的列表
                    // 获取列表 并完成注册/本地完成续签
                    List<ServerNode> allClientList = collectAllClientQueue(serverNodes);
                    if (CollUtil.isNotEmpty(allClientList)) {
                        waitRefreshDBClientNodes.addAll(allClientList);
                    }
                }

                if (CollUtil.isEmpty(waitRefreshDBClientNodes)) {
                    SnailJobLog.LOCAL.warn("clientNodes is empty");
                    return;
                }

                SnailJobLog.LOCAL.debug("start refresh client nodes：{}", waitRefreshDBClientNodes);

                // 刷新DB
                refreshExpireAt(waitRefreshDBClientNodes);

            } catch (Exception e) {
                SnailJobLog.LOCAL.error("refresh 失败", e);
            }
        }

        private List<ServerNode> collectAllClientQueue(List<ServerNode> serverNodes) {
            if (CollUtil.isEmpty(serverNodes)) {
                return Lists.newArrayList();
            }

            int size = serverNodes.size();
            // 存储处理结果
            List<Future<String>> futures = new ArrayList<>(size);
            for (ServerNode serverNode : serverNodes) {
                Future<String> future = executorService.submit(() -> {
                    try {
                        RegisterNodeInfo nodeInfo = new RegisterNodeInfo();
                        nodeInfo.setHostId(serverNode.getHostId());
                        nodeInfo.setGroupName(serverNode.getGroupName());
                        nodeInfo.setNamespaceId(serverNode.getNamespaceId());
                        nodeInfo.setHostPort(serverNode.getHostPort());
                        nodeInfo.setHostIp(serverNode.getHostIp());
                        CommonRpcClient serverRpcClient = buildRpcClient(nodeInfo);
                        Result<String> regNodesAndFlush = serverRpcClient.getRegNodesAndFlush();
                        return regNodesAndFlush.getData();
                    } catch (Exception e) {
                        return StrUtil.EMPTY;
                    }
                });

                futures.add(future);
            }

            return futures.stream()
                    .map(future -> {
                        try {
                            String jsonString = future.get(1, TimeUnit.SECONDS);
                            if (Objects.nonNull(jsonString)) {
                                return JsonUtil.parseList(jsonString, ServerNode.class);
                            }
                            return new ArrayList<ServerNode>();
                        } catch (Exception e) {
                            return new ArrayList<ServerNode>();
                        }
                    })
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .distinct()
                    .toList();

        }

        private CommonRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo) {

            int maxRetryTimes = 3;
            return RequestBuilder.<CommonRpcClient, Result>newBuilder()
                    .nodeInfo(registerNodeInfo)
                    .failRetry(true)
                    .retryTimes(maxRetryTimes)
                    .client(CommonRpcClient.class)
                    .build();
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
            taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT5S"));
        }
    }
}
