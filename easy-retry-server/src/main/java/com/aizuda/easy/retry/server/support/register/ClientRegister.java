package com.aizuda.easy.retry.server.support.register;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 客户端注册
 *
 * @author www.byteblogs.com
 * @date 2023-06-07
 * @since 1.6.0
 */
@Component
@Slf4j
public class ClientRegister extends AbstractRegister {
    public static final int DELAY_TIME = 30;
    protected static final LinkedBlockingQueue<ServerNode> QUEUE = new LinkedBlockingQueue<>();

    @Override
    public boolean supports(int type) {
        return getNodeType().equals(type);
    }

    @Override
    protected void beforeProcessor(RegisterContext context) {

    }

    @Override
    protected LocalDateTime getExpireAt(RegisterContext context) {
        return LocalDateTime.now().plusSeconds(DELAY_TIME);
    }

    @Override
    protected boolean doRegister(RegisterContext context, ServerNode serverNode) {

        Assert.isTrue(QUEUE.offer(serverNode), () ->
                new EasyRetryServerException("add register Queue error. groupName:[{}] size:[{}]",
                        serverNode.getGroupName(), QUEUE.size()));

        return Boolean.TRUE;
    }

    @Override
    protected Integer getNodeType() {
        return NodeTypeEnum.CLIENT.getType();
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (Thread.currentThread().isInterrupted()) {
                try {
                    ServerNode serverNode = QUEUE.take();
                    refreshExpireAt(serverNode);
                    // 防止刷的过快
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    LogUtils.error(log, "client refresh expireAt error.");
                }
            }
        }, "client_register_").start();
    }

    @Override
    public void close() {

    }
}
