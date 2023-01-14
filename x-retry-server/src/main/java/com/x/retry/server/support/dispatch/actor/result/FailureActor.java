package com.x.retry.server.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.retry.common.core.enums.RetryStatusEnum;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.util.Assert;
import com.x.retry.server.exception.XRetryServerException;
import com.x.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.x.retry.server.persistence.mybatis.po.SceneConfig;
import com.x.retry.server.persistence.support.ConfigAccess;
import com.x.retry.server.persistence.support.RetryTaskAccess;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 重试完成执行器
 * 1、更新重试任务
 * 2、记录重试日志
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component("FailureActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FailureActor extends AbstractActor {

    public static final String BEAN_NAME = "FailureActor";

    @Autowired
    @Qualifier("retryTaskAccessProcessor")
    private RetryTaskAccess<RetryTask> retryTaskAccess;

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTask.class, retryTask -> {
            LogUtils.info("FailureActor params:[{}]", retryTask);

            // 超过最大等级
            SceneConfig sceneConfig =
                    configAccess.getSceneConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName());

            if (sceneConfig.getMaxRetryCount() <= retryTask.getRetryCount()) {
                retryTask.setRetryStatus(RetryStatusEnum.MAX_RETRY_COUNT.getLevel());
            }

            try {
                retryTaskAccess.updateRetryTask(retryTask);
            } catch (Exception e) {
                LogUtils.error("更新重试任务失败", e);
            } finally {
                getContext().stop(getSelf());

                // 记录重试日志
                RetryTaskLog retryTaskLog = new RetryTaskLog();
                retryTaskLog.setRetryStatus(retryTask.getRetryStatus());
                Assert.isTrue(1 ==  retryTaskLogMapper.update(retryTaskLog,
                        new LambdaQueryWrapper<RetryTaskLog>().eq(RetryTaskLog::getBizId, retryTask.getBizId())),
                        new XRetryServerException("更新重试日志失败"));
            }

        }).build();

    }

}
