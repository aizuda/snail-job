package com.aizuda.snail.job.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.common.log.dto.TaskLogFieldDTO;
import com.aizuda.snail.job.server.common.akka.ActorGenerator;
import com.aizuda.snail.job.server.job.task.dto.JobLogDTO;
import com.aizuda.snail.job.server.job.task.support.JobTaskConverter;
import com.aizuda.snail.job.server.model.dto.JobLogTaskDTO;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snail.job.template.datasource.persistence.po.JobLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final JobLogMessageMapper jobLogMessageMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(List.class, (list -> {
                try {
                    if (CollectionUtils.isEmpty(list)) {
                        return;
                    }

                    List<JobLogTaskDTO> jobLogTasks = (List<JobLogTaskDTO>) list;
                    Map<Long, List<JobLogTaskDTO>> logTaskDTOMap = jobLogTasks.
                    stream().collect(Collectors.groupingBy(JobLogTaskDTO::getTaskId, Collectors.toList()));

                    List<JobLogMessage> jobLogMessageList = new ArrayList<>();
                    for (List<JobLogTaskDTO> logTaskDTOList : logTaskDTOMap.values()) {
                        JobLogMessage jobLogMessage = JobTaskConverter.INSTANCE.toJobLogMessage(logTaskDTOList.get(0));
                        jobLogMessage.setCreateDt(LocalDateTime.now());
                        jobLogMessage.setLogNum(logTaskDTOList.size());
                        List<Map<String, String>> messageMapList = logTaskDTOList.stream()
                            .map(taskDTO -> taskDTO.getFieldList()
                                .stream().filter(logTaskDTO_ -> !Objects.isNull(logTaskDTO_.getValue()))
                                .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue)))
                            .collect(Collectors.toList());
                        jobLogMessage.setMessage(JsonUtil.toJsonString(messageMapList));

                        jobLogMessageList.add(jobLogMessage);
                    }

                    jobLogMessageMapper.batchInsert(jobLogMessageList);
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
        jobLogMessage.setLogNum(1);
        jobLogMessage.setMessage(Optional.ofNullable(jobLogDTO.getMessage()).orElse(StrUtil.EMPTY));
        jobLogMessage.setTaskId(Optional.ofNullable(jobLogMessage.getTaskId()).orElse(0L));
        jobLogMessageMapper.insert(jobLogMessage);
    }
}
