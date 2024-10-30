package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.convert.JobConverter;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.HttpHeaderUtil;
import com.aizuda.snailjob.server.common.vo.JobRequestVO;
import com.aizuda.snailjob.server.job.task.support.cache.ResidentTaskCache;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * OPENAPI
 * 更新定时任务
 */
@Component
@RequiredArgsConstructor
public class OpenApiUpdateJobRequestHandler extends PostHttpRequestHandler {
    private final JobMapper jobMapper;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_UPDATE_JOB.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Update job content:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        String namespace = HttpHeaderUtil.getNamespace(headers);
        JobRequestVO jobRequestVO = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), JobRequestVO.class);
        if (Objects.isNull(jobRequestVO.getId())){
            SnailJobLog.LOCAL.warn("id不能为空，更新失败");
            return new SnailJobRpcResult(false, retryRequest.getReqId());
        }

        Job job = jobMapper.selectById(jobRequestVO.getId());
        if (Objects.isNull(job)){
            SnailJobLog.LOCAL.warn("job为空，更新失败");
            return new SnailJobRpcResult(false, retryRequest.getReqId());
        }

        // 判断常驻任务
        Job updateJob = JobConverter.INSTANCE.convert(jobRequestVO);
        updateJob.setResident(isResident(jobRequestVO));
        updateJob.setNamespaceId(namespace);

        // 工作流任务
        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            job.setNextTriggerAt(0L);
            // 非常驻任务 > 非常驻任务
        } else if (Objects.equals(job.getResident(), StatusEnum.NO.getStatus()) && Objects.equals(
                updateJob.getResident(),
                StatusEnum.NO.getStatus())) {
            updateJob.setNextTriggerAt(calculateNextTriggerAt(jobRequestVO, DateUtils.toNowMilli()));
        } else if (Objects.equals(job.getResident(), StatusEnum.YES.getStatus()) && Objects.equals(
                updateJob.getResident(), StatusEnum.NO.getStatus())) {
            // 常驻任务的触发时间
            long time = Optional.ofNullable(ResidentTaskCache.get(jobRequestVO.getId()))
                    .orElse(DateUtils.toNowMilli());
            updateJob.setNextTriggerAt(calculateNextTriggerAt(jobRequestVO, time));
            // 老的是不是常驻任务 新的是常驻任务 需要使用当前时间计算下次触发时间
        } else if (Objects.equals(job.getResident(), StatusEnum.NO.getStatus()) && Objects.equals(
                updateJob.getResident(), StatusEnum.YES.getStatus())) {
            updateJob.setNextTriggerAt(DateUtils.toNowMilli());
        }

        // 禁止更新组
        updateJob.setGroupName(null);
        boolean insert =  1 == jobMapper.updateById(updateJob);
        return new SnailJobRpcResult(insert, retryRequest.getReqId());

    }

    private Integer isResident(JobRequestVO jobRequestVO) {
        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return StatusEnum.NO.getStatus();
        }

        if (jobRequestVO.getTriggerType() == WaitStrategies.WaitStrategyEnum.FIXED.getType()) {
            if (Integer.parseInt(jobRequestVO.getTriggerInterval()) < 10) {
                return StatusEnum.YES.getStatus();
            }
        } else if (jobRequestVO.getTriggerType() == WaitStrategies.WaitStrategyEnum.CRON.getType()) {
            if (CronUtils.getExecuteInterval(jobRequestVO.getTriggerInterval()) < 10 * 1000) {
                return StatusEnum.YES.getStatus();
            }
        } else {
            throw new SnailJobServerException("未知触发类型");
        }

        return StatusEnum.NO.getStatus();
    }

    private static Long calculateNextTriggerAt(final JobRequestVO jobRequestVO, Long time) {
        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return 0L;
        }

        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(jobRequestVO.getTriggerType());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(jobRequestVO.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }
}
