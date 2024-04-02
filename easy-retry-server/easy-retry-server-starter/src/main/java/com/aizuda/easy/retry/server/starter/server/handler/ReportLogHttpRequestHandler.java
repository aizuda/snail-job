package com.aizuda.easy.retry.server.starter.server.handler;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.HeadersEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.log.enums.LogTypeEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.handler.PostHttpRequestHandler;
import com.aizuda.easy.retry.server.common.util.ClientInfoUtils;
import com.aizuda.easy.retry.server.model.dto.JobLogTaskDTO;
import com.aizuda.easy.retry.server.model.dto.RetryLogTaskDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

import static com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH.BATCH_LOG_REPORT;

/**
 * 处理日志上报数据
 *
 * @author: wodeyangzipingpingwuqi
 * @date : 2023-12-26
 * @since 1.0.0
 */
@Component
public class ReportLogHttpRequestHandler extends PostHttpRequestHandler {

    private static final String JSON_FILED = "logType" ;
    @Override
    public boolean supports(String path) {
        return BATCH_LOG_REPORT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public String doHandler(String content, UrlQuery urlQuery, HttpHeaders headers) {

        EasyRetryLog.LOCAL.debug("Begin Handler Log Report Data. [{}]", content);
        EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
        Object[] args = retryRequest.getArgs();

        Assert.notEmpty(args, () -> new EasyRetryServerException("日志上报的数据不能为空. ReqId:[{}]", retryRequest.getReqId()));

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
        if (!CollectionUtils.isEmpty(jobTasks)) {
            ActorRef actorRef = ActorGenerator.jobLogActor();
            actorRef.tell(jobTasks, actorRef);
        }

        if (!CollectionUtils.isEmpty(retryTasks)) {
            ActorRef actorRef = ActorGenerator.logActor();
            actorRef.tell(retryTasks, actorRef);
        }

        return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), "Batch Log Retry Data Upload Processed Successfully", Boolean.TRUE, retryRequest.getReqId()));
    }

    private String getClientInfo(final HttpHeaders headers) {
        String hostId = headers.get(HeadersEnum.HOST_ID.getKey());
        String hostIp = headers.get(HeadersEnum.HOST_IP.getKey());
        Integer hostPort = headers.getInt(HeadersEnum.HOST_PORT.getKey());
        RegisterNodeInfo registerNodeInfo = new RegisterNodeInfo();
        registerNodeInfo.setHostIp(hostIp);
        registerNodeInfo.setHostPort(hostPort);
        registerNodeInfo.setHostId(hostId);
        return ClientInfoUtils.generate(registerNodeInfo);
    }
}
