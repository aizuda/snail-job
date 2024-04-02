package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.log;

import akka.actor.AbstractActor;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.dto.TaskLogFieldDTO;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.model.dto.RetryLogTaskDTO;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 处理日志信息
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-16 11:33
 * @since 2.0.0
 */
@Component(ActorGenerator.LOG_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class RetryLogActor extends AbstractActor {
    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(List.class,
            list -> {
                if (CollectionUtils.isEmpty(list)) {
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
        Map<String, List<RetryLogTaskDTO>> logTaskDTOMap = jobLogTasks.
            stream().collect(Collectors.groupingBy(RetryLogTaskDTO::getUniqueId, Collectors.toList()));
        List<RetryTaskLogMessage> retryTaskLogMessages = new ArrayList<>();
        for (List<RetryLogTaskDTO> logTaskDTOList : logTaskDTOMap.values()) {
            RetryTaskLogMessage retryTaskLogMessage = RetryTaskConverter.INSTANCE.toRetryTaskLogMessage(
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

        retryTaskLogMessageMapper.batchInsert(retryTaskLogMessages);
    }

    /**
     * 报错日志详情
     */
    private void saveRetryTaskLogMessage(final RetryTaskLogDTO retryTaskLogDTO) {

        // 记录重试日志
        RetryTaskLogMessage retryTaskLogMessage = new RetryTaskLogMessage();
        retryTaskLogMessage.setUniqueId(retryTaskLogDTO.getUniqueId());
        retryTaskLogMessage.setGroupName(retryTaskLogDTO.getGroupName());
//        retryTaskLogMessage.setClientInfo(retryTaskLogDTO.getClientInfo());
        retryTaskLogMessage.setNamespaceId(retryTaskLogDTO.getNamespaceId());
        retryTaskLogMessage.setLogNum(1);
        retryTaskLogMessage.setRealTime(retryTaskLogDTO.getRealTime());
        String errorMessage = retryTaskLogDTO.getMessage();
        retryTaskLogMessage.setMessage(
                StrUtil.isBlank(errorMessage) ? StrUtil.EMPTY : errorMessage);
        retryTaskLogMessage.setCreateDt(Optional.ofNullable(retryTaskLogDTO.getTriggerTime()).orElse(LocalDateTime.now()));
        retryTaskLogMessageMapper.insert(retryTaskLogMessage);

    }
}
