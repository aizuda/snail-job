package com.aizuda.easy.retry.server.common.register;

import com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    protected static final LinkedBlockingDeque<ServerNode> QUEUE = new LinkedBlockingDeque<>(1000);

    @Override
    public boolean supports(int type) {
        return getNodeType().equals(type);
    }
    @Override
    protected void beforeProcessor(RegisterContext context) {
    }

    @Override
    protected LocalDateTime getExpireAt() {
        return LocalDateTime.now().plusSeconds(DELAY_TIME);
    }

    @Override
    protected boolean doRegister(RegisterContext context, ServerNode serverNode) {
        if (HTTP_PATH.BEAT.equals(context.getUri())) {
            return QUEUE.offerFirst(serverNode);
        }

        return QUEUE.offerLast(serverNode);
    }

    @Override
    protected void afterProcessor(final ServerNode serverNode) {

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
                    List<ServerNode> lists = Lists.newArrayList(serverNode);
                    QUEUE.drainTo(lists, 100);

                    // 去重
                    lists = new ArrayList<>(lists.stream()
                        .collect(
                            Collectors.toMap(ServerNode::getHostId, node -> node, (existing, replacement) -> existing))
                        .values());

                    refreshExpireAt(lists);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                EasyRetryLog.LOCAL.error("client refresh expireAt error.");
            } finally {
                // 防止刷的过快
                try {
                    TimeUnit.MILLISECONDS.sleep(5000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
