package com.aizuda.easy.retry.server.job.task.scan;

import akka.actor.AbstractActor;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:41
 */
@Component(ActorGenerator.SCAN_JOB_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class JobExecutorActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JobContext.class, jobContext -> {
            try {
                doExecute(jobContext);
            } catch (Exception e) {
                LogUtils.error(log, "job executor exception. [{}]", jobContext, e);
            }
        }).build();
    }

    private void doExecute(final JobContext jobContext) {
        // 调度客户端

    }
}
