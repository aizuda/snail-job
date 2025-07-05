package com.aizuda.snailjob.server.openapi.job.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HashUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobOpenApiResult;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.convert.JobConverter;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.HttpHeaderUtil;
import com.aizuda.snailjob.server.common.util.TriggerIntervalUtils;
import com.aizuda.snailjob.server.common.vo.JobRequestVO;
import com.aizuda.snailjob.server.openapi.job.convert.JobApiConverter;
import com.aizuda.snailjob.server.openapi.job.dto.JobRequestDTO;
import com.aizuda.snailjob.server.service.dto.JobRequestBaseDTO;
import com.aizuda.snailjob.server.service.kit.JobKit;
import com.aizuda.snailjob.server.service.kit.TriggerIntervalKit;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@Service
@RequiredArgsConstructor
public class JobApiService {
    private final SystemProperties systemProperties;
    private final JobMapper jobMapper;

    public Long addJob(@Valid JobRequestDTO jobRequest) {

        // 判断常驻任务
        Job job = JobApiConverter.INSTANCE.convert(jobRequest);
        job.setResident(JobKit.isResident(jobRequest.getTriggerType(), jobRequest.getTriggerInterval()));

        // check triggerInterval
        checkTriggerInterval(jobRequest);

        job.setBucketIndex(HashUtil.bkdrHash(jobRequest.getGroupName() + jobRequest.getJobName())
                % systemProperties.getBucketTotal());
        job.setNextTriggerAt(calculateNextTriggerAt(job, DateUtils.toNowMilli()));
        job.setId(null);
        Assert.isTrue(1 == jobMapper.insert(job), ()-> new SnailJobServerException("Adding new task failed"));
        return job.getId();
    }


private void checkTriggerInterval(JobRequestDTO jobRequestVO) {
    TriggerIntervalKit.checkTriggerInterval(jobRequestVO.getTriggerInterval(), jobRequestVO.getTriggerType());
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
