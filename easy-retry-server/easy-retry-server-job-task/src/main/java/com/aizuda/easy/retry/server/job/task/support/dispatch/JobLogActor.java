package com.aizuda.easy.retry.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.dto.JobLogDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobLogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author www.byteblogs.com
 * @date 2023-10-03 22:32:30
 * @since 2.4.0
 */
@Component(ActorGenerator.JOB_LOG_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class JobLogActor extends AbstractActor {

    @Autowired
    private JobLogMessageMapper jobLogMessageMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(List.class, (list -> {
                    try {
                        jobLogMessageMapper.batchInsert(list);
                    } catch (Exception e) {
                        log.error("保存客户端日志异常.", e);
                    } finally {
                        getContext().stop(getSelf());
                    }
                }))
                .match(JobLogDTO.class, (jobLogDTO -> {
                    try {
                        saveLogMessage(jobLogDTO);
                    } catch (Exception e) {
                        log.error("保存日志异常.", e);
                    } finally {
                        getContext().stop(getSelf());
                    }
                })).build();

    }

    private void saveLogMessage(JobLogDTO jobLogDTO) {
        JobLogMessage jobLogMessage = JobTaskConverter.INSTANCE.toJobLogMessage(jobLogDTO);
        jobLogMessage.setCreateDt(LocalDateTime.now());
        jobLogMessage.setMessage(Optional.ofNullable(jobLogDTO.getMessage()).orElse(StrUtil.EMPTY));
        jobLogMessage.setTaskId(Optional.ofNullable(jobLogMessage.getTaskId()).orElse(0L));
        jobLogMessageMapper.insert(jobLogMessage);
    }
}
