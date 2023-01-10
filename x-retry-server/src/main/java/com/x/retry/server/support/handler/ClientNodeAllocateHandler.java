package com.x.retry.server.support.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.x.retry.server.persistence.mybatis.po.GroupConfig;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import com.x.retry.server.persistence.support.ConfigAccess;
import com.x.retry.server.support.ClientLoadBalance;
import com.x.retry.server.support.allocate.client.ClientLoadBalanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
 * @date : 2023-01-10 14:18
 */
@Component
public class ClientNodeAllocateHandler {

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;
    @Autowired
    private ServerNodeMapper serverNodeMapper;

    /**
     * 获取分配的节点
     */
    public ServerNode getServerNode(RetryTask retryTask) {

        GroupConfig groupConfig = configAccess.getGroupConfigByGroupName(retryTask.getGroupName());
        List<ServerNode> serverNodes = serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getGroupName, retryTask.getGroupName()));

        if (CollectionUtils.isEmpty(serverNodes)) {
            return null;
        }

        ClientLoadBalance clientLoadBalanceRandom = ClientLoadBalanceManager.getClientLoadBalance(groupConfig.getRouteKey());

        String hostIp = clientLoadBalanceRandom.route(retryTask.getGroupName(), new TreeSet<>(serverNodes.stream().map(ServerNode::getHostIp).collect(Collectors.toSet())));
        return serverNodes.stream().filter(s -> s.getHostIp().equals(hostIp)).findFirst().get();
    }

}
