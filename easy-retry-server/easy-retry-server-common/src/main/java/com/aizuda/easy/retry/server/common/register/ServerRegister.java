package com.aizuda.easy.retry.server.common.register;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.Register;
import com.aizuda.easy.retry.server.common.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.ServerNodeExtAttrs;
import com.aizuda.easy.retry.server.common.handler.ServerNodeBalance;
import com.aizuda.easy.retry.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
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
@Component(ServerRegister.BEAN_NAME)
@Slf4j
public class ServerRegister extends AbstractRegister {
    public static final String BEAN_NAME = "serverRegister";
    private final ScheduledExecutorService serverRegisterNode = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r,"server-register-node"));
    public static final int DELAY_TIME = 30;
    public static final String CURRENT_CID;
    public static final String GROUP_NAME = "DEFAULT_SERVER";
    public static final String NAMESPACE_ID = "DEFAULT_SERVER_NAMESPACE_ID";

    @Autowired
    public ServerNodeBalance serverNodeBalance;
    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private ServerProperties serverProperties;

    static {
        CURRENT_CID = IdUtil.getSnowflakeNextIdStr();
    }

    @Override
    public boolean supports(int type) {
        return getNodeType().equals(type);
    }

    @Override
    protected void beforeProcessor(RegisterContext context) {
        // 新增扩展参数
        ServerNodeExtAttrs serverNodeExtAttrs = new ServerNodeExtAttrs();
        serverNodeExtAttrs.setWebPort(serverProperties.getPort());

        context.setGroupName(GROUP_NAME);
        context.setHostId(CURRENT_CID);
        context.setHostIp(HostUtils.getIp());
        context.setHostPort(systemProperties.getNettyPort());
        context.setContextPath(Optional.ofNullable(serverProperties.getServlet().getContextPath()).orElse(StrUtil.EMPTY));
        context.setNamespaceId(NAMESPACE_ID);
        context.setExtAttrs(JsonUtil.toJsonString(serverNodeExtAttrs));
    }

    @Override
    protected LocalDateTime getExpireAt() {
        return LocalDateTime.now().plusSeconds(DELAY_TIME);
    }

    @Override
    protected boolean doRegister(RegisterContext context, ServerNode serverNode) {
        refreshExpireAt(Lists.newArrayList(serverNode));
        return Boolean.TRUE;
    }


    @Override
    protected void afterProcessor(final ServerNode serverNode) {
        try {
            // 同步当前POD消费的组的节点信息
            // netty的client只会注册到一个服务端，若组分配的和client连接的不是一个POD则会导致当前POD没有其他客户端的注册信息
            ConcurrentMap<String /*groupName*/, String/*namespaceId*/> allConsumerGroupName = CacheConsumerGroup.getAllConsumerGroupName();
            if (!CollectionUtils.isEmpty(allConsumerGroupName)) {
                List<ServerNode> serverNodes = serverNodeMapper.selectList(
                    new LambdaQueryWrapper<ServerNode>()
                        .eq(ServerNode::getNodeType, NodeTypeEnum.CLIENT.getType())
                        .in(ServerNode::getNamespaceId, new HashSet<>(allConsumerGroupName.values()))
                        .in(ServerNode::getGroupName, allConsumerGroupName.keySet()));
                for (final ServerNode node : serverNodes) {
                    // 刷新全量本地缓存
                    CacheRegisterTable.addOrUpdate(node);
                }
            }
        } catch (Exception e) {
            LogUtils.error(log, "刷新客户端失败", e);
        }
    }

    @Override
    protected Integer getNodeType() {
        return NodeTypeEnum.SERVER.getType();
    }

    @Override
    public void start() {
        LogUtils.info(log, "ServerRegister start");

        Register register = SpringContext.getBean(ServerRegister.BEAN_NAME, Register.class);
        serverRegisterNode.scheduleAtFixedRate(()->{
            register.register(new RegisterContext());
        }, 0, DELAY_TIME / 2, TimeUnit.SECONDS);

    }

    @Override
    public void close() {
        LogUtils.info(log, "ServerRegister close");
    }
}
