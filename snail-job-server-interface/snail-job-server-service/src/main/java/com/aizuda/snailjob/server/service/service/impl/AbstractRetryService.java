package com.aizuda.snailjob.server.service.service.impl;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.model.RetryArgsDeserializeDTO;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.enums.RetryTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.service.convert.RetryConverter;
import com.aizuda.snailjob.server.service.dto.RetryResponseDTO;
import com.aizuda.snailjob.server.service.dto.StatusUpdateRequestDTO;
import com.aizuda.snailjob.server.service.dto.TriggerRetryDTO;
import com.aizuda.snailjob.server.service.handler.RetryArgsDeserializeHandler;
import com.aizuda.snailjob.server.service.service.RetryService;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.pekko.actor.ActorRef;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-25
 */
public abstract class AbstractRetryService implements RetryService {
    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private RetryArgsDeserializeHandler retryArgsDeserializeHandler;

    @Override
    public <T extends RetryResponseDTO> T getRetryById(Long retryId, Class<T> clazz) {
        Retry retry = accessTemplate.getRetryAccess().one(new LambdaQueryWrapper<Retry>().eq(Retry::getId, retryId));

        Assert.notNull(retry, () -> new SnailJobServerException("Retry task not found:[{}].", retryId));

        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
            RetryConverter.INSTANCE.toRetryResponseVO(retry, instance);

            RetryArgsDeserializeDTO deserializeDTO = new RetryArgsDeserializeDTO();
            deserializeDTO.setArgsStr(retry.getArgsStr());
            deserializeDTO.setExecutorName(retry.getExecutorName());
            deserializeDTO.setScene(retry.getSceneName());
            deserializeDTO.setGroup(retry.getGroupName());
            deserializeDTO.setSerializerName(retry.getSerializerName());
            deserializeDTO.setNamespaceId(retry.getNamespaceId());
            instance.setArgsStr(retryArgsDeserializeHandler.deserialize(deserializeDTO));
        } catch (Exception e) {
            throw new SnailJobServerException("Failed to get retry by id [{}]", retryId, e);
        }

        return instance;
    }

    @Override
    public boolean triggerRetry(TriggerRetryDTO triggerRetryDTO) {
        Retry retry = accessTemplate.getRetryAccess().one(new LambdaQueryWrapper<Retry>().eq(Retry::getId, triggerRetryDTO.getId()));
        Assert.notNull(retry, () -> new SnailJobServerException("Retry task not found:[{}].", triggerRetryDTO.getId()));
        long count = accessTemplate.getGroupConfigAccess().count(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getGroupName, retry.getGroupName())
                .eq(GroupConfig::getNamespaceId, getNamespaceId())
                .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
        );

        Assert.isTrue(count > 0, () -> new SnailJobServerException("Group [{}] is closed, manual execution is not supported.", retry.getGroupName()));

        Assert.isTrue(Objects.equals(retry.getTaskType(), SyetemTaskTypeEnum.RETRY.getType()), () -> new SnailJobServerException("No executable tasks"));

        RetryTaskPrepareDTO retryTaskPrepareDTO = com.aizuda.snailjob.server.retry.task.convert.RetryConverter.INSTANCE.toRetryTaskPrepareDTO(retry);
        // 设置now表示立即执行
        retryTaskPrepareDTO.setNextTriggerAt(DateUtils.toNowMilli());
        retryTaskPrepareDTO.setRetryTaskExecutorScene(RetryTaskExecutorSceneEnum.MANUAL_RETRY.getScene());
        retryTaskPrepareDTO.setRetryId(retry.getId());
        // 准备阶段执行
        ActorRef actorRef = ActorGenerator.retryTaskPrepareActor();
        actorRef.tell(retryTaskPrepareDTO, actorRef);

        return false;
    }

    @Override
    public boolean updateRetryStatus(StatusUpdateRequestDTO requestDTO) {
        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(requestDTO.getStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new SnailJobServerException("Retry status error. [{}]", requestDTO.getStatus());
        }

        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
        Retry retry = retryTaskAccess.one(new LambdaQueryWrapper<Retry>()
                .eq(Retry::getNamespaceId, getNamespaceId())
                .eq(Retry::getId, requestDTO.getId()));
        if (Objects.isNull(retry)) {
            throw new SnailJobServerException("Retry task not found");
        }

        retry.setRetryStatus(requestDTO.getStatus());

        // 若恢复重试则需要重新计算下次触发时间
        if (RetryStatusEnum.RUNNING.getStatus().equals(retryStatusEnum.getStatus())) {

            RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess()
                    .getSceneConfigByGroupNameAndSceneName(retry.getGroupName(), retry.getSceneName(), getNamespaceId());
            WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
            waitStrategyContext.setNextTriggerAt(DateUtils.toNowMilli());
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
            waitStrategyContext.setDelayLevel(retry.getRetryCount() + 1);
            WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff());
            retry.setNextTriggerAt(waitStrategy.computeTriggerTime(waitStrategyContext));
            retry.setDeleted(0L);
        }

        if (RetryStatusEnum.FINISH.getStatus().equals(retryStatusEnum.getStatus())) {
            retry.setDeleted(retry.getId());
        }

        retry.setUpdateDt(LocalDateTime.now());

        return retryTaskAccess.updateById(retry) == 1;
    }

    protected abstract String getNamespaceId();
}
