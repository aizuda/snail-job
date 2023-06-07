package com.aizuda.easy.retry.server.support.register;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.aizuda.easy.retry.server.support.Register;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

/**
 * @author www.byteblogs.com
 * @date 2023-06-07
 * @since 1.6.0
 */
@Slf4j
public abstract class AbstractRegister implements Register, Lifecycle {

    @Autowired
    private ServerNodeMapper serverNodeMapper;
    @Autowired
    private SystemProperties systemProperties;

    @Override
    public boolean register(RegisterContext context) {

        beforeProcessor(context);

        ServerNode serverNode = initServerNode(context);

        return doRegister(context, serverNode);
    }

    protected void refreshExpireAt(ServerNode serverNode) {

        try {
            serverNodeMapper.insertOrUpdate(serverNode);
        }catch (Exception e) {
            LogUtils.error(log,"注册节点失败 groupName:[{}] hostIp:[{}]",
                    serverNode.getGroupName(), serverNode.getHostIp(), e);
        }
    }

    protected abstract void beforeProcessor(RegisterContext context);

    protected ServerNode initServerNode(RegisterContext context) {

        ServerNode serverNode = new ServerNode();
        serverNode.setHostId(context.getHostId());
        serverNode.setHostIp(context.getHostIp());
        serverNode.setGroupName(context.getGroupName());
        serverNode.setHostPort(systemProperties.getNettyPort());
        serverNode.setNodeType(getNodeType());
        serverNode.setCreateDt(LocalDateTime.now());
        serverNode.setContextPath(context.getContextPath());
        serverNode.setExpireAt(getExpireAt(context));

        return serverNode;
    }

    protected abstract LocalDateTime getExpireAt(RegisterContext context);


    protected abstract boolean doRegister(RegisterContext context, ServerNode serverNode);


    protected abstract Integer getNodeType();



}
