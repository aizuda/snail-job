package com.aizuda.snailjob.server.retry.task.support.dispatch;

import  org.apache.pekko.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.server.common.enums.RetryTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryPrePareHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum.NOT_COMPLETE;
import static com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum.SUCCESS;
import static com.aizuda.snailjob.server.common.pekko.ActorGenerator.RETRY_TASK_PREPARE_ACTOR;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@Slf4j
@Component(RETRY_TASK_PREPARE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class RetryTaskPrepareActor extends AbstractActor {
    private final List<RetryPrePareHandler> retryPrePareHandlers;
    private final RetryTaskMapper retryTaskMapper;


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTaskPrepareDTO.class, prepareDTO -> {

            try {
                doPrepare(prepareDTO);
            } catch (Exception e) {
                log.error("预处理节点异常", e);
            } finally {
                getContext().stop(getSelf());
            }

        }).build();
    }

    /**
     * 对数据进行预处理
     *
     * @param prepareDTO RetryTaskPrepareDTO
     */
    private void doPrepare(RetryTaskPrepareDTO prepareDTO) {

        List<RetryTask> retryTasks = retryTaskMapper.selectList(
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getRetryId, prepareDTO.getRetryId())
                        .in(RetryTask::getTaskStatus, NOT_COMPLETE)
                        .orderByAsc(RetryTask::getRetryId)
        );

        if (CollUtil.isEmpty(retryTasks)
                || Objects.isNull(prepareDTO.getRetryTaskExecutorScene())
                || RetryTaskExecutorSceneEnum.MANUAL_RETRY.getScene() == prepareDTO.getRetryTaskExecutorScene()) {
            RetryTask retryTask = new RetryTask();
            retryTask.setTaskStatus(SUCCESS.getStatus());
            retryTasks = Lists.newArrayList(retryTask);
        }

        boolean onlyTimeoutCheck = false;
        for (RetryTask retryTask : retryTasks) {
            prepareDTO.setRetryTaskId(retryTask.getId());
            prepareDTO.setOnlyTimeoutCheck(onlyTimeoutCheck);
            for (RetryPrePareHandler retryPrePareHandler : retryPrePareHandlers) {
                if (retryPrePareHandler.matches(retryTask.getTaskStatus())) {
                    retryPrePareHandler.handle(prepareDTO);
                    break;
                }
            }

            // 当存在大量待处理任务时，除了第一个任务需要执行阻塞策略，其他任务只做任务检查
            onlyTimeoutCheck = true;
        }
    }
}
