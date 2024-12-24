//package com.aizuda.snailjob.server.starter.schedule;
//
//import cn.hutool.core.collection.CollUtil;
//import com.aizuda.snailjob.common.core.enums.NodeTypeEnum;
//import com.aizuda.snailjob.common.core.model.Result;
//import com.aizuda.snailjob.common.core.util.JsonUtil;
//import com.aizuda.snailjob.common.core.util.NetUtil;
//import com.aizuda.snailjob.common.log.SnailJobLog;
//import com.aizuda.snailjob.server.common.Lifecycle;
//import com.aizuda.snailjob.server.common.config.SystemProperties;
//import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
//import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
//import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
//import com.aizuda.snailjob.server.common.triple.Pair;
//import com.aizuda.snailjob.server.job.task.server.ServerRpcClient;
//import com.aizuda.snailjob.server.job.task.support.request.GetRegNodesPostHttpRequestHandler;
//import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
//import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Sets;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//import java.util.concurrent.*;
//import java.util.stream.Collectors;
//
//import static com.aizuda.snailjob.server.common.register.ClientRegister.DELAY_TIME;
//
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class RefreshNodeSchedule extends AbstractSchedule implements Lifecycle {
//    private final ServerNodeMapper serverNodeMapper;
//
//    private final SystemProperties systemProperties;
//
//    ExecutorService executorService = Executors.newFixedThreadPool(5);
//
//    @Override
//    protected void doExecute() {
//        int nettyPort = systemProperties.getNettyPort();
//        String localIpStr = NetUtil.getLocalIpStr();
//        try {
//            // 获取在线的客户端节点并且排除当前节点
//            LambdaQueryWrapper<ServerNode> wrapper = new LambdaQueryWrapper<ServerNode>()
//                    .eq(ServerNode::getNodeType, NodeTypeEnum.SERVER.getType())
//                    .not(w -> w.eq(ServerNode::getHostIp, localIpStr)
//                            .eq(ServerNode::getHostPort, nettyPort));
//            List<ServerNode> serverNodes = serverNodeMapper.selectList(wrapper);
//            List<ServerNode> clientNodes = new ArrayList<>();
//            if (serverNodes.size() > 0) {
//                // 并行获取所有服务端需要注册的列表
//                // 获取列表 并完成注册/本地完成续签
//                List<ServerNode> allClientList = getAllClientList(serverNodes);
//                if (CollUtil.isNotEmpty(allClientList)) {
//                    clientNodes.addAll(allClientList);
//                }
//                List<ServerNode> refreshCache = GetRegNodesPostHttpRequestHandler.getAndRefreshCache();
//                if (CollUtil.isNotEmpty(refreshCache)) {
//                    // 完成本节点的刷新
//                    clientNodes.addAll(refreshCache);
//                }
//            } else {
//                List<ServerNode> refreshCache = GetRegNodesPostHttpRequestHandler.getAndRefreshCache();
//                if (CollUtil.isNotEmpty(refreshCache)) {
//                    // 完成本节点的刷新
//                    clientNodes.addAll(refreshCache);
//                }
//            }
//            if (CollUtil.isEmpty(clientNodes)) {
//                SnailJobLog.LOCAL.warn("clientNodes is empty");
//                return;
//            }
//            SnailJobLog.LOCAL.info("start refresh client nodes：{}", clientNodes);
//            refreshExpireAt(clientNodes);
//
//        } catch (Exception e) {
//            SnailJobLog.LOCAL.error("refresh 失败", e);
//        }
//    }
//
//    private List<ServerNode> getAllClientList(List<ServerNode> serverNodes) {
//        int size = serverNodes.size();
//        // 创建 CountDownLatch
//        CountDownLatch latch = new CountDownLatch(size);
//
//        // 存储处理结果
//        List<Future<String>> futures = new ArrayList<>(size);
//
//        try {
//            for (ServerNode serverNode : serverNodes) {
//                Future<String> future = executorService.submit(() -> {
//                    try {
//                        RegisterNodeInfo nodeInfo = new RegisterNodeInfo();
//                        nodeInfo.setHostId(serverNode.getHostId());
//                        nodeInfo.setGroupName(serverNode.getGroupName());
//                        nodeInfo.setNamespaceId(serverNode.getNamespaceId());
//                        nodeInfo.setHostPort(serverNode.getHostPort());
//                        nodeInfo.setHostIp(serverNode.getHostIp());
//                        ServerRpcClient serverRpcClient = buildRpcClient(nodeInfo);
//                        Result<String> regNodesAndFlush = serverRpcClient.getRegNodesAndFlush();
//
//                        // 模拟耗时处理
//                        return regNodesAndFlush.getData();
//                    } finally {
//                        // 处理完成后计数减一
//                        latch.countDown();
//                    }
//                });
//                futures.add(future);
//            }
//            // 提交任务
//
//            // 等待所有任务完成
//            latch.await(5, TimeUnit.SECONDS);  // 设置超时时间为5秒
//
//            return futures.stream()
//                    .map(future -> {
//                        try {
//                            String jsonString = future.get(1, TimeUnit.SECONDS);
//                            if (Objects.nonNull(jsonString)) {
//                                return JsonUtil.parseObject(jsonString, new TypeReference<List<ServerNode>>() {
//                                });
//                            }
//                            return new ArrayList<ServerNode>();
//                        } catch (Exception e) {
//                            return new ArrayList<ServerNode>();
//                        }
//                    })
//                    .filter(Objects::nonNull)
//                    .flatMap(List::stream)
//                    .distinct()
//                    .toList();
//            // 收集处理结果
//
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public String lockName() {
//        return "registerNode";
//    }
//
//    @Override
//    public String lockAtMost() {
//        return "PT10S";
//    }
//
//    @Override
//    public String lockAtLeast() {
//        return "PT5S";
//    }
//
//    @Override
//    public void start() {
//        taskScheduler.scheduleWithFixedDelay(this::execute, Instant.now(), Duration.parse("PT5S"));
//    }
//
//    @Override
//    public void close() {
//
//    }
//
//    private ServerRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo) {
////        String regInfo = registerNodeInfo.getHostId() + "/" + registerNodeInfo.getHostIp() + "/" + registerNodeInfo.getHostPort();
////        log.info(regInfo + "--------------------------");
//        int maxRetryTimes = 3;
//        boolean retry = false;
//        return RequestBuilder.<ServerRpcClient, Result>newBuilder()
//                .nodeInfo(registerNodeInfo)
//                .failRetry(maxRetryTimes > 0 && !retry)
//                .retryTimes(maxRetryTimes)
//                .client(ServerRpcClient.class)
//                .build();
//    }
//
//    private void refreshExpireAt(List<ServerNode> serverNodes) {
//        if (CollUtil.isEmpty(serverNodes)) {
//            return;
//        }
//
//        Set<String> hostIds = Sets.newHashSet();
//        Set<String> hostIps = Sets.newHashSet();
//        for (final ServerNode serverNode : serverNodes) {
//            serverNode.setExpireAt(getExpireAt());
//            hostIds.add(serverNode.getHostId());
//            hostIps.add(serverNode.getHostIp());
//        }
//
//        List<ServerNode> dbServerNodes = serverNodeMapper.selectList(
//                new LambdaQueryWrapper<ServerNode>()
//                        .select(ServerNode::getHostIp, ServerNode::getHostId)
//                        .in(ServerNode::getHostId, hostIds)
//                        .in(ServerNode::getHostIp, hostIps)
//        );
//
//        List<ServerNode> insertDBs = Lists.newArrayList();
//        List<ServerNode> updateDBs = Lists.newArrayList();
//        Set<Pair<String, String>> pairs = dbServerNodes.stream()
//                .map(serverNode -> Pair.of(serverNode.getHostId(), serverNode.getHostIp())).collect(
//                        Collectors.toSet());
//
//        // 去重处理
//        Set<Pair<String, String>> existed = Sets.newHashSet();
//        for (final ServerNode serverNode : serverNodes) {
//            Pair<String, String> pair = Pair.of(serverNode.getHostId(), serverNode.getHostIp());
//            if (existed.contains(pair)) {
//                continue;
//            }
//
//            if (pairs.contains(pair)) {
//                updateDBs.add(serverNode);
//            } else {
//                insertDBs.add(serverNode);
//            }
//
//            existed.add(pair);
//        }
//
//        try {
//            // 批量更新
//            if (CollUtil.isNotEmpty(updateDBs)) {
//                serverNodeMapper.updateBatchExpireAt(updateDBs);
//            }
//        } catch (Exception e) {
//            SnailJobLog.LOCAL.error("续租失败", e);
//        }
//
//        try {
//            if (CollUtil.isNotEmpty(insertDBs)) {
//                serverNodeMapper.insertBatch(insertDBs);
//            }
//        } catch (DuplicateKeyException ignored) {
//        } catch (Exception e) {
//            SnailJobLog.LOCAL.error("注册节点失败", e);
//        }
//    }
//
//    private LocalDateTime getExpireAt() {
//        return LocalDateTime.now().plusSeconds(DELAY_TIME);
//    }
//}
