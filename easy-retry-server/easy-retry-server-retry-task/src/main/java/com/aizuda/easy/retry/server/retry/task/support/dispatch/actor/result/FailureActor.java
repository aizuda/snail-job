package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.CallbackTimerTask;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.RetryTimerContext;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.RetryTimerTask;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.TimerWheelHandler;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.log.RetryTaskLogDTO;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskActuatorSceneEnum;
import com.aizuda.easy.retry.server.retry.task.support.handler.CallbackRetryTaskHandler;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * 重试完成执行器 1、更新重试任务 2、记录重试日志
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component(ActorGenerator.FAILURE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class FailureActor extends AbstractActor {

    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private CallbackRetryTaskHandler callbackRetryTaskHandler;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private SystemProperties systemProperties;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTask.class, retryTask -> {
            LogUtils.info(log, "FailureActor params:[{}]", retryTask);

            // 超过最大等级
            SceneConfig sceneConfig =
                    accessTemplate.getSceneConfigAccess().getSceneConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName());

            try {
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {

                        RetryTimerContext timerContext = new RetryTimerContext();
                        timerContext.setGroupName(retryTask.getGroupName());
                        timerContext.setUniqueId(retryTask.getUniqueId());

                        TimerTask timerTask = null;
                        Integer maxRetryCount;
                        if (TaskTypeEnum.CALLBACK.getType().equals(retryTask.getTaskType())) {
                            maxRetryCount = systemProperties.getCallback().getMaxCount();
                            timerTask = new CallbackTimerTask();
                            timerContext.setScene(TaskActuatorSceneEnum.AUTO_CALLBACK);
                        } else {
                            maxRetryCount = sceneConfig.getMaxRetryCount();
                            timerTask = new RetryTimerTask(timerContext);
                            timerContext.setScene(TaskActuatorSceneEnum.AUTO_RETRY);
                        }

                        if (maxRetryCount <= retryTask.getRetryCount()) {
                            retryTask.setRetryStatus(RetryStatusEnum.MAX_COUNT.getStatus());
                            // 创建一个回调任务
                            callbackRetryTaskHandler.create(retryTask);
                        } else {
                            // TODO 计算延迟的时间 此处需要判断符合条件的才会进入时间轮
                            LocalDateTime nextTriggerAt = retryTask.getNextTriggerAt();
                            long delay = nextTriggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();
                            log.info("准确进入时间轮 {} {}", nextTriggerAt, delay);
                            TimerWheelHandler.register(retryTask.getGroupName(), retryTask.getUniqueId(), timerTask, delay, TimeUnit.MILLISECONDS);
                        }

                        retryTask.setUpdateDt(LocalDateTime.now());
                        Assert.isTrue(1 == accessTemplate.getRetryTaskAccess()
                                        .updateById(retryTask.getGroupName(), retryTask),
                                () -> new EasyRetryServerException("更新重试任务失败. groupName:[{}] uniqueId:[{}]",
                                        retryTask.getGroupName(), retryTask.getUniqueId()));
                    }
                });
            } catch (Exception e) {
                LogUtils.error(log, "更新重试任务失败", e);
            } finally {

                if (RetryStatusEnum.MAX_COUNT.getStatus().equals(retryTask.getRetryStatus())) {
                    RetryTaskLogDTO retryTaskLogDTO = new RetryTaskLogDTO();
                    retryTaskLogDTO.setGroupName(retryTask.getGroupName());
                    retryTaskLogDTO.setUniqueId(retryTask.getUniqueId());
                    retryTaskLogDTO.setRetryStatus(retryTask.getRetryStatus());
                    retryTaskLogDTO.setMessage("任务已经到达最大执行次数了.");
                    ActorRef actorRef = ActorGenerator.logActor();
                    actorRef.tell(retryTaskLogDTO, actorRef);
                }

                getContext().stop(getSelf());
            }

        }).build();

    }

}
