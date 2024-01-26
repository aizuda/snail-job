package com.aizuda.easy.retry.server.starter.server.handler;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.log.dto.TaskLogFieldDTO;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.handler.PostHttpRequestHandler;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.model.dto.LogTaskDTO;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobLogMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

        EasyRetryLog.LOCAL.info("Begin Handler Log Report Data. [{}]", content);
        EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);
        Object[] args = retryRequest.getArgs();

        Assert.notEmpty(args, () -> new EasyRetryServerException("日志上报的数据不能为空. ReqId:[{}]", retryRequest.getReqId()));
        Map<Long, List<LogTaskDTO>> logTaskDTOMap = JsonUtil.parseList(JsonUtil.toJsonString(args[0]), LogTaskDTO.class).stream().collect(Collectors.groupingBy(i -> i.getTaskId(), Collectors.toList()));

        List<JobLogMessage> jobLogMessageList = new ArrayList<>();
        for (List<LogTaskDTO> logTaskDTOList : logTaskDTOMap.values()) {

            JobLogMessage jobLogMessage = JobTaskConverter.INSTANCE.toJobLogMessage(logTaskDTOList.get(0));
            jobLogMessage.setCreateDt(LocalDateTime.now());
            jobLogMessage.setLogNum(logTaskDTOList.size());
            List<Map<String, String>> messageMapList = logTaskDTOList.stream().map(taskDTO -> taskDTO.getFieldList()
                    .stream().filter(logTaskDTO_ -> !Objects.isNull(logTaskDTO_.getValue()))
                    .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue))).collect(Collectors.toList());
            jobLogMessage.setMessage(JsonUtil.toJsonString(messageMapList));

            jobLogMessageList.add(jobLogMessage);
        }

        // 批量新增日志数据
        ActorRef actorRef = ActorGenerator.jobLogActor();
        actorRef.tell(jobLogMessageList, actorRef);
        return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), "Batch Log Retry Data Upload Processed Successfully", Boolean.TRUE, retryRequest.getReqId()));
    }
}
