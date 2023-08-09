package com.aizuda.easy.retry.server.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.support.RetryTaskAccess;
import com.aizuda.easy.retry.server.support.dispatch.actor.log.RetryTaskLogDTO;
import com.aizuda.easy.retry.server.support.handler.CallbackRetryTaskHandler;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 重试完成执行器 1、更新重试任务 2、记录重试日志
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component("FailureActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class FailureActor extends AbstractActor {

    public static final String BEAN_NAME = "FailureActor";

    @Autowired
    @Qualifier("retryTaskAccessProcessor")
    private RetryTaskAccess<RetryTask> retryTaskAccess;
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

                        Integer maxRetryCount;
                        if (TaskTypeEnum.CALLBACK.getType().equals(retryTask.getTaskType())) {
                            maxRetryCount = systemProperties.getCallback().getMaxCount();
                        } else {
                            maxRetryCount = sceneConfig.getMaxRetryCount();
                        }

                        if (maxRetryCount <= retryTask.getRetryCount()) {
                            retryTask.setRetryStatus(RetryStatusEnum.MAX_COUNT.getStatus());
                            // 创建一个回调任务
                            callbackRetryTaskHandler.create(retryTask);
                        }

                        Assert.isTrue(1 == retryTaskAccess.updateRetryTask(retryTask), () ->
                            new EasyRetryServerException("更新重试任务失败. groupName:[{}] uniqueId:[{}]",
                                retryTask.getGroupName(),  retryTask.getUniqueId()));
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
