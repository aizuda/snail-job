package com.aizuda.easy.retry.server.support.register;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.support.Register;
import com.aizuda.easy.retry.server.support.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.support.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.support.handler.ServerNodeBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务端注册
 *
 * @author www.byteblogs.com
 * @date 2023-06-07
 * @since 1.6.0
 */
@Component
public class ServerRegister extends AbstractRegister {

    private final ScheduledExecutorService serverRegisterNode = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r,"ServerRegisterNode"));
    public static final int DELAY_TIME = 30;
    public static final String CURRENT_CID;
    public static final String GROUP_NAME = "DEFAULT_SERVER";
    @Autowired
    public ServerNodeBalance serverNodeBalance;

    static {
        CURRENT_CID = IdUtil.simpleUUID();
    }

    @Override
    public boolean supports(int type) {
        return getNodeType().equals(type);
    }


    @Override
    protected void beforeProcessor(RegisterContext context) {
        context.setGroupName(GROUP_NAME);
        context.setHostId(CURRENT_CID);
        context.setHostIp(HostUtils.getIp());
        context.setGroupName(StrUtil.EMPTY);
        context.setContextPath(StrUtil.EMPTY);
    }

    @Override
    protected LocalDateTime getExpireAt(RegisterContext context) {
        return LocalDateTime.now().plusSeconds(DELAY_TIME);
    }

    @Override
    protected boolean doRegister(RegisterContext context, ServerNode serverNode) {
        refreshExpireAt(serverNode);
        Set<ServerNode> serverNodeSet = CacheRegisterTable.getServerNodeSet(context.getGroupName());
        for (final ServerNode node : serverNodeSet) {
            ServerNode consumerServerNode = CacheConsumerGroup.get(node.getHostId());
            if (consumerServerNode.getExpireAt().isBefore(LocalDateTime.now())) {
                // 触发rebalance
                serverNodeBalance.doBalance();
            }
        }
        return Boolean.TRUE;
    }

    @Override
    protected Integer getNodeType() {
        return NodeTypeEnum.SERVER.getType();
    }

    @Override
    public void start() {
        Register register = SpringContext.getBean("serverRegister", Register.class);
        serverRegisterNode.scheduleAtFixedRate(()->{
            register.register(new RegisterContext());
        }, 1, DELAY_TIME / 2, TimeUnit.SECONDS);

    }

    @Override
    public void close() {

    }
}
