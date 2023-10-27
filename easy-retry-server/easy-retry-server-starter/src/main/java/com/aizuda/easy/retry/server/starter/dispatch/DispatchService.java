package com.aizuda.easy.retry.server.starter.dispatch;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.dto.DistributeInstance;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 负责EasyRetry系统任务分发调度
 *
 * @author www.byteblogs.com
 * @date 2023-09-21 23:36:47
 * @since 2.4.0
 */
@Component
@Slf4j
public class DispatchService implements Lifecycle {

    /**
     * 分配器线程
     */
    private final ScheduledExecutorService dispatchService = Executors
            .newSingleThreadScheduledExecutor(r -> new Thread(r, "dispatch-service"));

    /**
     * 调度时长
     */
    public static final Long PERIOD = 10L;

    /**
     * 延迟30s为了尽可能保障集群节点都启动完成在进行rebalance
     */
    public static final Long INITIAL_DELAY = 30L;

    @Override
    public void start() {

        // TODO待优化
        ActorRef actorRef = ActorGenerator.scanBucketActor();

        dispatchService.scheduleAtFixedRate(() -> {

            try {
                // 当正在rebalance时延迟10s，尽量等待所有节点都完成rebalance
                if (DistributeInstance.RE_BALANCE_ING.get()) {
                    LogUtils.info(log, "正在rebalance中....");
                    TimeUnit.SECONDS.sleep(INITIAL_DELAY);
                }

                Set<Integer> currentConsumerBuckets = getConsumerBucket();
                if (!CollectionUtils.isEmpty(currentConsumerBuckets)) {
                    ConsumerBucket scanTaskDTO = new ConsumerBucket();
                    scanTaskDTO.setBuckets(currentConsumerBuckets);
                    actorRef.tell(scanTaskDTO, actorRef);
                }

            } catch (Exception e) {
                LogUtils.error(log, "分发异常", e);
            }


        }, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
    }


    /**
     * 分配当前POD负责消费的桶
     *
     * @return {@link  GroupConfig} 组上下文
     */
    private Set<Integer> getConsumerBucket() {
        return DistributeInstance.INSTANCE.getConsumerBucket();
    }

    @Override
    public void close() {

    }
}
