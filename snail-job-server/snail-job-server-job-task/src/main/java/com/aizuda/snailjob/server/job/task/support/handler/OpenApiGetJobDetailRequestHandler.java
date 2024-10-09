package com.aizuda.snailjob.server.job.task.support.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.convert.JobResponseVOConverter;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.vo.JobResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OPENAPI
 * 获取定时任务详情
 */
@Component
@RequiredArgsConstructor
public class OpenApiGetJobDetailRequestHandler extends PostHttpRequestHandler {
    private final JobMapper jobMapper;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_GET_JOB_DETAIL.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public String doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Update job content:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        Long jobId = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), Long.class);
        Assert.notNull(jobId, () -> new SnailJobServerException("id 不能为空"));

        Job job = jobMapper.selectById(jobId);
        JobResponseVO convert = JobResponseVOConverter.INSTANCE.convert(job);
        return JsonUtil.toJsonString(new NettyResult(convert, retryRequest.getReqId()));

    }
}
