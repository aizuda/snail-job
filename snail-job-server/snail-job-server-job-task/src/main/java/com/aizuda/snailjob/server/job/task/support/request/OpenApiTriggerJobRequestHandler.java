package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.JobTriggerDTO;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.JobPrepareHandler;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * OPENAPI
 * 调度定时任务
 */
@Component
@RequiredArgsConstructor
public class OpenApiTriggerJobRequestHandler extends PostHttpRequestHandler {
    private final JobMapper jobMapper;
    private final AccessTemplate accessTemplate;
    private final JobPrepareHandler terminalJobPrepareHandler;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_TRIGGER_JOB.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Trigger job content:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        JobTriggerDTO jobTriggerDTO = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), JobTriggerDTO.class);
        Job job = jobMapper.selectById(jobTriggerDTO.getJobId());
        Assert.notNull(job, () -> new SnailJobServerException("job can not be null."));

        long count = accessTemplate.getGroupConfigAccess().count(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getGroupName, job.getGroupName())
                .eq(GroupConfig::getNamespaceId, job.getNamespaceId())
                .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
        );

        if (count <= 0){
            SnailJobLog.LOCAL.warn("Group [{}] is closed, manual execution is not supported.", job.getGroupName());
            return new SnailJobRpcResult(false, retryRequest.getReqId());
        }
        JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(job);
        // 设置now表示立即执行
        jobTaskPrepare.setNextTriggerAt(DateUtils.toNowMilli());
        jobTaskPrepare.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_JOB.getType());
        // 设置手动参数
        if (StrUtil.isNotBlank(jobTriggerDTO.getTmpArgsStr())) {
            jobTaskPrepare.setTmpArgsStr(jobTriggerDTO.getTmpArgsStr());
        }
        // 创建批次
        terminalJobPrepareHandler.handle(jobTaskPrepare);

        return new SnailJobRpcResult(true, retryRequest.getReqId());
    }
}
