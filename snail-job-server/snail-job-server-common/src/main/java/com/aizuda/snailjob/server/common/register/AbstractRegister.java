package com.aizuda.snailjob.server.common.register;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.Register;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author opensnail
 * @date 2023-06-07
 * @since 1.6.0
 */
@Slf4j
public abstract class AbstractRegister implements Register, Lifecycle {

    @Autowired
    protected ServerNodeMapper serverNodeMapper;

    @Override
    public boolean register(RegisterContext context) {

        beforeProcessor(context);

        ServerNode serverNode = initServerNode(context);

        boolean result = doRegister(context, serverNode);

        afterProcessor(serverNode);

        return result;
    }

    protected abstract void afterProcessor(final ServerNode serverNode);

    protected void refreshExpireAt(List<ServerNode> serverNodes) {
        if (CollUtil.isEmpty(serverNodes)) {
            return;
        }

        Set<String> hostIds = Sets.newHashSet();
        Set<String> hostIps = Sets.newHashSet();
        for (final ServerNode serverNode : serverNodes) {
            serverNode.setExpireAt(getExpireAt());
             hostIds.add(serverNode.getHostId());
             hostIps.add(serverNode.getHostIp());
        }

        List<ServerNode> dbServerNodes = serverNodeMapper.selectList(
            new LambdaQueryWrapper<ServerNode>()
                .select(ServerNode::getHostIp, ServerNode::getHostId)
                .in(ServerNode::getHostId, hostIds)
                .in(ServerNode::getHostIp, hostIps)
        );

        List<ServerNode> insertDBs = Lists.newArrayList();
        List<ServerNode> updateDBs = Lists.newArrayList();
        Set<Pair<String, String>> pairs = dbServerNodes.stream()
            .map(serverNode -> Pair.of(serverNode.getHostId(), serverNode.getHostIp())).collect(
                Collectors.toSet());

        // 去重处理
        Set<Pair<String, String>> existed = Sets.newHashSet();
        for (final ServerNode serverNode : serverNodes) {
            Pair<String, String> pair = Pair.of(serverNode.getHostId(), serverNode.getHostIp());
            if (existed.contains(pair)) {
                continue;
            }

            if (pairs.contains(pair)) {
                updateDBs.add(serverNode);
            } else {
                insertDBs.add(serverNode);
            }

            existed.add(pair);
        }

        try {
            // 批量更新
            if (updateDBs.size() != serverNodeMapper.updateBatchExpireAt(updateDBs)) {
                SnailJobLog.LOCAL.warn("续租失败 [{}]", JsonUtil.toJsonString(updateDBs));
            }
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("续租失败", e);
        }

        try {
            if (insertDBs.size() != serverNodeMapper.insertBatch(insertDBs)) {
                SnailJobLog.LOCAL.warn("注册节点失败 [{}]", JsonUtil.toJsonString(insertDBs));
            }
        } catch (DuplicateKeyException ignored) {
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("注册节点失败", e);
        }

        for (final ServerNode serverNode : serverNodes) {
            // 刷新本地缓存过期时间
            CacheRegisterTable.refreshExpireAt(serverNode);
        }

    }

    protected abstract void beforeProcessor(RegisterContext context);

    protected ServerNode initServerNode(RegisterContext context) {

        ServerNode serverNode = new ServerNode();
        serverNode.setHostId(context.getHostId());
        serverNode.setHostIp(context.getHostIp());
        serverNode.setNamespaceId(context.getNamespaceId());
        serverNode.setGroupName(context.getGroupName());
        serverNode.setHostPort(context.getHostPort());
        serverNode.setNodeType(getNodeType());
        serverNode.setCreateDt(LocalDateTime.now());
        serverNode.setExtAttrs(context.getExtAttrs());

        return serverNode;
    }

    protected abstract LocalDateTime getExpireAt();


    protected abstract boolean doRegister(RegisterContext context, ServerNode serverNode);


    protected abstract Integer getNodeType();


}
