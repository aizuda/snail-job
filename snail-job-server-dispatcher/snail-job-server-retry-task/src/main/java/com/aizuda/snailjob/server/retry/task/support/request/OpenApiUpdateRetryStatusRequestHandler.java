package com.aizuda.snailjob.server.retry.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.vo.RequestUpdateRetryStatusVO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Deprecated
public class OpenApiUpdateRetryStatusRequestHandler extends PostHttpRequestHandler {

    private final RetryMapper retryMapper;

    private final AccessTemplate accessTemplate;

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("update retry status:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        RequestUpdateRetryStatusVO updateRetryStatusVO = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), RequestUpdateRetryStatusVO.class);

        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(updateRetryStatusVO.getRetryStatus());
        Assert.notNull(retryStatusEnum, () -> new SnailJobServerException("Retry status error. [{}]", updateRetryStatusVO.getRetryStatus()));

        Retry retry = retryMapper.selectById(updateRetryStatusVO.getId());
        Assert.notNull(retry, () -> new SnailJobServerException("Retry task not found:[{}].", retry.getId()));

        retry.setRetryStatus(updateRetryStatusVO.getRetryStatus());

        // 若恢复重试则需要重新计算下次触发时间
        if (RetryStatusEnum.RUNNING == retryStatusEnum) {
            RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess()
                    .getSceneConfigByGroupNameAndSceneName(retry.getGroupName(), retry.getSceneName(), retry.getNamespaceId());
            WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
            waitStrategyContext.setNextTriggerAt(DateUtils.toNowMilli());
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
            waitStrategyContext.setDelayLevel(retry.getRetryCount() + 1);
            WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff());
            retry.setNextTriggerAt(waitStrategy.computeTriggerTime(waitStrategyContext));
        }

        retry.setUpdateDt(LocalDateTime.now());

        Assert.isTrue(retryMapper.updateById(retry) == 1, () -> new SnailJobServerException("Update status of retry task failed:[{}].", retry.getId()));

        return new SnailJobRpcResult(true, retryRequest.getReqId());
    }

    @Override
    public boolean supports(String path) {
        return SystemConstants.HTTP_PATH.OPENAPI_UPDATE_RETRY_STATUS.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }
}
