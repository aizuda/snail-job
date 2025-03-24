package com.aizuda.snailjob.server.job.task.support.dispatch;

import com.aizuda.snailjob.server.common.dto.JobLogDTO;
import  org.apache.pekko.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.service.LogService;
import com.aizuda.snailjob.server.model.dto.JobLogTaskDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author opensnail
 * @date 2023-10-03 22:32:30
 * @since 2.4.0
 */
@Component(ActorGenerator.JOB_LOG_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class JobLogActor extends AbstractActor {
    private final LogService logService;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(List.class, (list -> {
                    try {
                        if (CollUtil.isEmpty(list)) {
                            return;
                        }

                        List<JobLogTaskDTO> jobLogTasks = (List<JobLogTaskDTO>) list;
                        logService.batchSaveLogs(jobLogTasks);
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
        logService.saveLog(jobLogDTO);
    }
}
