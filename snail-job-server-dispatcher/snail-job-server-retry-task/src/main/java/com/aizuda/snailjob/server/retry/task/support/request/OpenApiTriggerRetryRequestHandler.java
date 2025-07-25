package com.aizuda.snailjob.server.retry.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.enums.RetryTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.vo.RequestTriggerRetryVO;
import com.aizuda.snailjob.server.retry.task.convert.RetryConverter;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.apache.pekko.actor.ActorRef;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Deprecated
public class OpenApiTriggerRetryRequestHandler extends PostHttpRequestHandler {

    private final RetryMapper retryMapper;

    private final AccessTemplate accessTemplate;

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("trigger retry:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        RequestTriggerRetryVO triggerRetryVO = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), RequestTriggerRetryVO.class);

        Retry retry = retryMapper.selectById(triggerRetryVO.getId());

        Assert.notNull(retry, () -> new SnailJobServerException("Retry task not found:[{}].", triggerRetryVO.getId()));

        long count = accessTemplate.getGroupConfigAccess().count(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getGroupName, retry.getGroupName())
                .eq(GroupConfig::getNamespaceId, retry.getNamespaceId())
                .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
        );

        Assert.isTrue(count > 0, () -> new SnailJobServerException("Group [{}] is closed, manual execution is not supported.", retry.getGroupName()));

        Assert.isTrue(Objects.equals(retry.getTaskType(), SyetemTaskTypeEnum.RETRY.getType()), () -> new SnailJobServerException("No executable tasks"));

        RetryTaskPrepareDTO retryTaskPrepareDTO = RetryConverter.INSTANCE.toRetryTaskPrepareDTO(retry);
        // 设置now表示立即执行
        retryTaskPrepareDTO.setNextTriggerAt(DateUtils.toNowMilli());
        retryTaskPrepareDTO.setRetryTaskExecutorScene(RetryTaskExecutorSceneEnum.MANUAL_RETRY.getScene());
        retryTaskPrepareDTO.setRetryId(retry.getId());
        // 准备阶段执行
        ActorRef actorRef = ActorGenerator.retryTaskPrepareActor();
        actorRef.tell(retryTaskPrepareDTO, actorRef);
        return new SnailJobRpcResult(true, retryRequest.getReqId());
    }

    @Override
    public boolean supports(String path) {
        return SystemConstants.HTTP_PATH.OPENAPI_TRIGGER_RETRY.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }
}
