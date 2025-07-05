package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.convert.JobConverter;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.HttpHeaderUtil;
import com.aizuda.snailjob.server.common.util.TriggerIntervalUtils;
import com.aizuda.snailjob.server.common.vo.JobRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * OPENAPI
 * 新增定时任务
 */
@Component
@RequiredArgsConstructor
@Deprecated
public class OpenApiAddJobRequestHandler extends PostHttpRequestHandler {
    private final SystemProperties systemProperties;
    private final JobMapper jobMapper;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_ADD_JOB.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
            SnailJobLog.LOCAL.debug("Add job content:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        JobRequestVO jobRequestVO = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), JobRequestVO.class);
        if(StrUtil.isBlank(jobRequestVO.getGroupName())){
            jobRequestVO.setGroupName(HttpHeaderUtil.getGroupName(headers));
        }

        // 判断常驻任务
        Job job = JobConverter.INSTANCE.convert(jobRequestVO);
        job.setResident(isResident(jobRequestVO));

        // check triggerInterval
        checkTriggerInterval(jobRequestVO);

        job.setBucketIndex(HashUtil.bkdrHash(jobRequestVO.getGroupName() + jobRequestVO.getJobName())
                % systemProperties.getBucketTotal());
        job.setNextTriggerAt(calculateNextTriggerAt(job, DateUtils.toNowMilli()));

        job.setNamespaceId(HttpHeaderUtil.getNamespace(headers));
        job.setId(null);
        Assert.isTrue(1 == jobMapper.insert(job), ()-> new SnailJobServerException("Adding new task failed"));
        return new SnailJobRpcResult(job.getId(), retryRequest.getReqId());
    }

    private void checkTriggerInterval(JobRequestVO jobRequestVO) {
        TriggerIntervalUtils.checkTriggerInterval(jobRequestVO.getTriggerInterval(), jobRequestVO.getTriggerType());
    }

    private Integer isResident(JobRequestVO jobRequestVO) {
        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return StatusEnum.NO.getStatus();
        }

        if (Objects.equals(jobRequestVO.getTriggerType(), WaitStrategies.WaitStrategyEnum.FIXED.getType())) {
            if (Integer.parseInt(jobRequestVO.getTriggerInterval()) < 10) {
                return StatusEnum.YES.getStatus();
            }
        } else if (Objects.equals(jobRequestVO.getTriggerType(), WaitStrategies.WaitStrategyEnum.CRON.getType())) {
            if (CronUtils.getExecuteInterval(jobRequestVO.getTriggerInterval()) < 10 * 1000) {
                return StatusEnum.YES.getStatus();
            }
        } else if (Objects.equals(jobRequestVO.getTriggerType(), WaitStrategies.WaitStrategyEnum.POINT_IN_TIME.getType())) {
            return StatusEnum.NO.getStatus();
        } else {
            throw new SnailJobServerException("Unknown trigger type");
        }

        return StatusEnum.NO.getStatus();
    }

    private static Long calculateNextTriggerAt(final Job job, Long time) {
        if (Objects.equals(job.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return 0L;
        }

        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(job.getTriggerType());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(job.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }
}
