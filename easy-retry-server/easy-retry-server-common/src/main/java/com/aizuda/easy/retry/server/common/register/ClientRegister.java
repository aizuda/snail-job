package com.aizuda.easy.retry.server.common.register;

import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 客户端注册
 *
 * @author www.byteblogs.com
 * @date 2023-06-07
 * @since 1.6.0
 */
@Component(ClientRegister.BEAN_NAME)
@Slf4j
public class ClientRegister extends AbstractRegister implements Runnable {

    public static final String BEAN_NAME = "clientRegister";

    public static final int DELAY_TIME = 30;
    private Thread THREAD = null;
    protected static final LinkedBlockingQueue<ServerNode> QUEUE = new LinkedBlockingQueue<>(256);

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
        return QUEUE.offer(serverNode);
    }

    @Override
    protected Integer getNodeType() {
        return NodeTypeEnum.CLIENT.getType();
    }

    @Override
    public void start() {
        THREAD = new Thread(this, "client-register");
        THREAD.start();
    }

    @Override
    public void close() {
        if (Objects.nonNull(THREAD)) {
            THREAD.interrupt();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ServerNode serverNode = QUEUE.poll(5L, TimeUnit.SECONDS);
                if (Objects.nonNull(serverNode)) {
                    refreshExpireAt(serverNode);
                }

            }catch (InterruptedException e) {
                LogUtils.info(log, "[{}] thread stop.", Thread.currentThread().getName());
            } catch (Exception e) {
                LogUtils.error(log, "client refresh expireAt error.");
            } finally {
                // 防止刷的过快
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
