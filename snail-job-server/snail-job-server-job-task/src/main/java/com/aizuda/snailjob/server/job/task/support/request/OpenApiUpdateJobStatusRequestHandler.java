package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.vo.JobStatusUpdateRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OPENAPI
 * 更新定时任务状态
 */
@Component
@RequiredArgsConstructor
public class OpenApiUpdateJobStatusRequestHandler extends PostHttpRequestHandler {
    private final JobMapper jobMapper;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_UPDATE_JOB_STATUS.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        JobStatusUpdateRequestVO jobRequestVO = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), JobStatusUpdateRequestVO.class);
        Long count = jobMapper.selectCount(new LambdaQueryWrapper<Job>().eq(Job::getId, jobRequestVO.getId()));
        if (1 != count){
            SnailJobLog.LOCAL.warn("Updating task failed");
            return new SnailJobRpcResult(false, retryRequest.getReqId());
        }
        Job job = new Job();
        job.setId(jobRequestVO.getId());
        job.setJobStatus(jobRequestVO.getJobStatus());
        boolean update = 1 == jobMapper.updateById(job);
        return new SnailJobRpcResult(update, retryRequest.getReqId());

    }
}
