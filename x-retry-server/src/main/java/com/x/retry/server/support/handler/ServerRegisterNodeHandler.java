package com.x.retry.server.support.handler;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.retry.common.core.enums.NodeTypeEnum;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.util.HostUtils;
import com.x.retry.server.config.SystemProperties;
import com.x.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import com.x.retry.server.support.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务端注册处理器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 14:49
 */
@Component
public class ServerRegisterNodeHandler implements Lifecycle {

    @Autowired
    private ServerNodeMapper serverNodeMapper;
    @Autowired
    private SystemProperties systemProperties;

    private final ScheduledExecutorService serverRegisterNode = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r,"ServerRegisterNode"));
    public static final int DELAY_TIME = 20;
    public static final String CURRENT_CID;

    static {
        CURRENT_CID = IdUtil.simpleUUID();
    }

    @Override
    public void start() {

        ServerNode serverNode = new ServerNode();
        serverNode.setHostId(CURRENT_CID);
        serverNode.setHostIp(HostUtils.getIp());
        serverNode.setGroupName(StrUtil.EMPTY);
        serverNode.setHostPort(systemProperties.getNettyPort());
        serverNode.setNodeType(NodeTypeEnum.SERVER.getType());
        serverNode.setCreateDt(LocalDateTime.now());
        serverRegisterNode.scheduleAtFixedRate(()->{

            try {
                serverNode.setExpireAt(LocalDateTime.now().plusSeconds(DELAY_TIME));
                serverNodeMapper.insertOrUpdate(serverNode);
            }catch (Exception e) {
                LogUtils.error("服务端注册节点失败", e);
            }

        }, 1, DELAY_TIME / 2, TimeUnit.SECONDS);

    }

    @Override
    public void close() {
        LogUtils.info("准备删除节点 [{}]", CURRENT_CID);
        int i = serverNodeMapper.delete(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getHostId, CURRENT_CID));
        if (1 == i) {
            LogUtils.info("删除节点 [{}]成功", CURRENT_CID);
        } else {
            LogUtils.info("删除节点 [{}]失败", CURRENT_CID);
        }

    }
}
