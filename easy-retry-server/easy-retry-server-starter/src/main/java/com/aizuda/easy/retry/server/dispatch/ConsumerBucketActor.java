package com.aizuda.easy.retry.server.dispatch;

import akka.actor.AbstractActor;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 消费当前节点分配的bucket并生成扫描任务
 * <p>
 *
 * @author www.byteblogs.com
 * @date 2023-09-21 23:47:23
 * @since 2.4.0
 */
@Component(ActorGenerator.SCAN_BUCKET_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ConsumerBucketActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return null;
    }
}
