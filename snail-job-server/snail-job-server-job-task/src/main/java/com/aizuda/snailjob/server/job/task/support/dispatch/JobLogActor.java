package com.aizuda.snailjob.server.job.task.support.dispatch;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.dto.TaskLogFieldDTO;
import com.aizuda.snailjob.server.common.dto.JobLogDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.JobLogMessageDO;
import  org.apache.pekko.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.model.dto.JobLogTaskDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
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
    private final AccessTemplate accessTemplate;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(List.class, (list -> {
                    try {
                        if (CollUtil.isEmpty(list)) {
                            return;
                        }

                        List<JobLogTaskDTO> jobLogTasks = (List<JobLogTaskDTO>) list;
                        Map<Long, List<JobLogTaskDTO>> logTaskDTOMap = jobLogTasks.
                                stream().collect(Collectors.groupingBy(JobLogTaskDTO::getTaskId, Collectors.toList()));
                        List<JobLogMessageDO> jobLogMessageList = new ArrayList<>();
                        for (List<JobLogTaskDTO> logTaskDTOList : logTaskDTOMap.values()) {
                            JobLogMessageDO jobLogMessage = JobTaskConverter.INSTANCE.toJobLogMessage(logTaskDTOList.get(0));
                            jobLogMessage.setLogNum(logTaskDTOList.size());
                            List<Map<String, String>> messageMapList = StreamUtils.toList(logTaskDTOList,
                                    taskDTO -> taskDTO.getFieldList()
                                            .stream().filter(logTaskDTO_ -> !Objects.isNull(logTaskDTO_.getValue()))
                                            .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue)));
                            jobLogMessage.setMessage(JsonUtil.toJsonString(messageMapList));
                            jobLogMessageList.add(jobLogMessage);
                        }

                        accessTemplate.getJobLogMessageAccess().insertBatch(jobLogMessageList);
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
        JobLogMessageDO jobLogMessage = JobTaskConverter.INSTANCE.toJobLogMessage(jobLogDTO);
        jobLogMessage.setLogNum(1);
        jobLogMessage.setMessage(Optional.ofNullable(jobLogDTO.getMessage()).orElse(StrUtil.EMPTY));
        jobLogMessage.setTaskId(Optional.ofNullable(jobLogMessage.getTaskId()).orElse(0L));
        accessTemplate.getJobLogMessageAccess().insert(jobLogMessage);
    }
}
