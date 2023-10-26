package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.log;

import akka.actor.AbstractActor;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLogMessage;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 处理日志信息
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-16 11:33
 * @since 2.0.0
 */
@Component(ActorGenerator.LOG_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class LogActor extends AbstractActor {

    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTaskLogDTO.class,
            retryTaskLogDTO -> RetryStatusEnum.RUNNING.getStatus().equals(retryTaskLogDTO.getRetryStatus()),
            retryTaskLogDTO -> {
                saveRetryTaskLogMessage(retryTaskLogDTO);
                getContext().stop(getSelf());
            }).match(RetryTaskLogDTO.class, (retryTaskLogDTO) ->
                RetryStatusEnum.MAX_COUNT.getStatus().equals(retryTaskLogDTO.getRetryStatus())
                    || RetryStatusEnum.FINISH.getStatus().equals(retryTaskLogDTO.getRetryStatus()),
            retryTaskLogDTO -> {

                // 变动日志的状态
                RetryTaskLog retryTaskLog = new RetryTaskLog();
                retryTaskLog.setRetryStatus(retryTaskLogDTO.getRetryStatus());
                retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
                    .eq(RetryTaskLog::getUniqueId, retryTaskLogDTO.getUniqueId())
                    .eq(RetryTaskLog::getGroupName, retryTaskLogDTO.getGroupName()));

                getContext().stop(getSelf());
            }).build();
    }

    /**
     * 报错日志详情
     */
    private void saveRetryTaskLogMessage(final RetryTaskLogDTO retryTaskLogDTO) {

        // 记录重试日志
        RetryTaskLogMessage retryTaskLogMessage = new RetryTaskLogMessage();
        retryTaskLogMessage.setUniqueId(retryTaskLogDTO.getUniqueId());
        retryTaskLogMessage.setGroupName(retryTaskLogDTO.getGroupName());
        String errorMessage = retryTaskLogDTO.getMessage();
        retryTaskLogMessage.setMessage(
            StrUtil.isBlank(errorMessage) ? StrUtil.EMPTY : errorMessage);
        retryTaskLogMessage.setCreateDt(Optional.ofNullable(retryTaskLogDTO.getTriggerTime()).orElse(LocalDateTime.now()));
        retryTaskLogMessageMapper.insert(retryTaskLogMessage);

    }
}
