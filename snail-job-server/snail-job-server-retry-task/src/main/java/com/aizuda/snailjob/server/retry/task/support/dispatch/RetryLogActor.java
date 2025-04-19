package com.aizuda.snailjob.server.retry.task.support.dispatch;

import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.RetryTaskLogMessageDO;
import  org.apache.pekko.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.dto.TaskLogFieldDTO;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.model.dto.RetryLogTaskDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskLogDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理日志信息
 *
 * @author: opensnail
 * @date : 2023-06-16 11:33
 * @since 2.0.0
 */
@Component(ActorGenerator.LOG_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class RetryLogActor extends AbstractActor {
    private final AccessTemplate accessTemplate;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(List.class,
                list -> {
                    if (CollUtil.isEmpty(list)) {
                        return;
                    }
                    saveRetryTaskLogMessage((List<RetryLogTaskDTO>) list);
                    getContext().stop(getSelf());
                }).match(RetryTaskLogDTO.class,
                retryTaskLogDTO -> {
                    saveRetryTaskLogMessage(retryTaskLogDTO);
                    getContext().stop(getSelf());
                }).build();
    }

    private void saveRetryTaskLogMessage(final List<RetryLogTaskDTO> list) {

        List<RetryLogTaskDTO> jobLogTasks = list;
        Map<Long, List<RetryLogTaskDTO>> logTaskDTOMap = jobLogTasks.
                stream().collect(Collectors.groupingBy(RetryLogTaskDTO::getRetryTaskId, Collectors.toList()));
        List<RetryTaskLogMessageDO> retryTaskLogMessages = new ArrayList<>();
        for (List<RetryLogTaskDTO> logTaskDTOList : logTaskDTOMap.values()) {
            RetryTaskLogMessageDO retryTaskLogMessage = RetryTaskConverter.INSTANCE.toRetryTaskLogMessage(
                    logTaskDTOList.get(0));
            retryTaskLogMessage.setCreateDt(LocalDateTime.now());
            retryTaskLogMessage.setLogNum(logTaskDTOList.size());
            List<Map<String, String>> messageMapList = logTaskDTOList.stream()
                    .map(taskDTO -> taskDTO.getFieldList()
                            .stream().filter(logTaskDTO_ -> !Objects.isNull(logTaskDTO_.getValue()))
                            .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue)))
                    .collect(Collectors.toList());
            retryTaskLogMessage.setMessage(JsonUtil.toJsonString(messageMapList));

            retryTaskLogMessages.add(retryTaskLogMessage);
        }

        accessTemplate.getRetryTaskLogMessageAccess().insertBatch(retryTaskLogMessages);
    }

    /**
     * 报错日志详情
     */
    private void saveRetryTaskLogMessage(final RetryTaskLogDTO retryTaskLogDTO) {

        // 记录重试日志
        RetryTaskLogMessageDO retryTaskLogMessage = new RetryTaskLogMessageDO();
        retryTaskLogMessage.setRetryId(retryTaskLogDTO.getRetryId());
        retryTaskLogMessage.setRetryTaskId(retryTaskLogDTO.getRetryTaskId());
        retryTaskLogMessage.setGroupName(retryTaskLogDTO.getGroupName());
        retryTaskLogMessage.setNamespaceId(retryTaskLogDTO.getNamespaceId());
        retryTaskLogMessage.setLogNum(1);
        retryTaskLogMessage.setRealTime(retryTaskLogDTO.getRealTime());
        String errorMessage = retryTaskLogDTO.getMessage();
        retryTaskLogMessage.setMessage(
                StrUtil.isBlank(errorMessage) ? StrUtil.EMPTY : errorMessage);
        retryTaskLogMessage.setCreateDt(Optional.ofNullable(retryTaskLogDTO.getTriggerTime()).orElse(LocalDateTime.now()));
        accessTemplate.getRetryTaskLogMessageAccess().insert(retryTaskLogMessage);

    }
}
