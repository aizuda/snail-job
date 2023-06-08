package com.aizuda.easy.retry.server.support.handler;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.support.allocate.server.AllocateMessageQueueConsistentHash;
import com.aizuda.easy.retry.server.support.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.support.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.support.register.ServerRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author: shuguang.zhang
 * @date : 2023-06-08 15:58
 */
@Component
@Slf4j
public class ServerNodeBalance {

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    public void doBalance() {

        // 已经按照id 正序排序
        List<GroupConfig> prepareAllocateGroupConfig = configAccess.getAllOpenGroupConfig();
        if (CollectionUtils.isEmpty(prepareAllocateGroupConfig)) {
            return;
        }

        // 为了保证客户端分配算法的一致性,serverNodes 从数据库从数据获取
        Set<String> podIpSet = CacheRegisterTable.getPodIdSet(ServerRegister.GROUP_NAME);

        if (CollectionUtils.isEmpty(podIpSet)) {
            LogUtils.error(log, "服务端节点为空");
            return;
        }

        Set<String> groupNameSet = prepareAllocateGroupConfig.stream().map(GroupConfig::getGroupName)
            .collect(Collectors.toSet());

        List<String> allocate = new AllocateMessageQueueConsistentHash()
            .allocate(ServerRegisterNodeHandler.CURRENT_CID, new ArrayList<>(groupNameSet), new ArrayList<>(podIpSet));
        for (final String groupName : allocate) {
            CacheConsumerGroup.addOrUpdate(groupName);

        }
    }
}
