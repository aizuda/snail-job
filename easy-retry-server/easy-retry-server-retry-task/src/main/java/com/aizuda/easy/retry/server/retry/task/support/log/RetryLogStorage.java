package com.aizuda.easy.retry.server.retry.task.support.log;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.dto.LogContentDTO;
import com.aizuda.easy.retry.common.log.dto.TaskLogFieldDTO;
import com.aizuda.easy.retry.common.log.enums.LogTypeEnum;
import com.aizuda.easy.retry.server.common.LogStorage;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.dto.LogMetaDTO;
import com.aizuda.easy.retry.server.common.log.LogStorageFactory;
import com.aizuda.easy.retry.server.common.dto.RetryLogMetaDTO;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.log.RetryTaskLogDTO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: xiaowoniu
 * @date : 2024-03-22
 * @since : 3.2.0
 */
@Component
public class RetryLogStorage implements LogStorage, InitializingBean {

    @Override
    public LogTypeEnum logType() {
        return LogTypeEnum.RETRY;
    }

    @Override
    public void storage(final LogContentDTO logContentDTO, final LogMetaDTO logMetaDTO) {
        RetryLogMetaDTO retryLogMetaDTO = (RetryLogMetaDTO) logMetaDTO;
        RetryTaskLogDTO retryTaskLogDTO = new RetryTaskLogDTO();
        Map<String, String> messageMap = logContentDTO.getFieldList()
            .stream()
            .filter(logTaskDTO_ -> !Objects.isNull(logTaskDTO_.getValue()))
            .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue));
        retryTaskLogDTO.setMessage(JsonUtil.toJsonString(Lists.newArrayList(messageMap)));
        retryTaskLogDTO.setGroupName(retryLogMetaDTO.getGroupName());
        retryTaskLogDTO.setNamespaceId(retryLogMetaDTO.getNamespaceId());
        retryTaskLogDTO.setUniqueId(retryLogMetaDTO.getUniqueId());
        retryTaskLogDTO.setRealTime(retryLogMetaDTO.getTimestamp());
        ActorRef actorRef = ActorGenerator.logActor();
        actorRef.tell(retryTaskLogDTO, actorRef);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LogStorageFactory.register(logType(), this);
    }
}
