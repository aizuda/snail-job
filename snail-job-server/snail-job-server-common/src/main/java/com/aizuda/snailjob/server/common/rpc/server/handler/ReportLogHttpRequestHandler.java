package com.aizuda.snailjob.server.common.rpc.server.handler;

import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.model.dto.JobLogTaskDTO;
import com.aizuda.snailjob.server.model.dto.RetryLogTaskDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.BATCH_LOG_REPORT;

/**
 * 处理日志上报数据
 *
 * @author: wodeyangzipingpingwuqi
 * @date : 2023-12-26
 * @since 1.0.0
 */
@Component
public class ReportLogHttpRequestHandler extends PostHttpRequestHandler {
    @Override
    public boolean supports(String path) {
        return BATCH_LOG_REPORT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {

        SnailJobLog.LOCAL.debug("Begin Handler Log Report Data. [{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();

        Assert.notEmpty(args, () -> new SnailJobServerException("日志上报的数据不能为空. ReqId:[{}]", retryRequest.getReqId()));

        JsonNode jsonNode = JsonUtil.toJson(args[0]);
        List<RetryLogTaskDTO> retryTasks = Lists.newArrayList();
        List<JobLogTaskDTO> jobTasks = Lists.newArrayList();
        for (final JsonNode node : jsonNode) {
            JsonNode value = node.findValue(SystemConstants.JSON_FILED_LOG_TYPE);
            if (Objects.isNull(value) || value.asText().equals(LogTypeEnum.JOB.name())) {
                jobTasks.add(JsonUtil.parseObject(node.toPrettyString(), JobLogTaskDTO.class));
                continue;
            }

            if (value.asText().equals(LogTypeEnum.RETRY.name())) {
                retryTasks.add(JsonUtil.parseObject(node.toPrettyString(), RetryLogTaskDTO.class));
            }
        }

        // 批量新增日志数据
        if (CollUtil.isNotEmpty(jobTasks)) {
            ActorRef actorRef = ActorGenerator.jobLogActor();
            actorRef.tell(jobTasks, actorRef);
        }

        if (CollUtil.isNotEmpty(retryTasks)) {
            ActorRef actorRef = ActorGenerator.logActor();
            actorRef.tell(retryTasks, actorRef);
        }

        return new SnailJobRpcResult(StatusEnum.YES.getStatus(), "Batch Log Retry Data Upload Processed Successfully", Boolean.TRUE, retryRequest.getReqId());
    }

}
