package com.aizuda.snailjob.template.datasource.access.log.config;

import com.aizuda.snailjob.template.datasource.access.JobLogAccess;
import com.aizuda.snailjob.template.datasource.access.RetryLogAccess;
import com.aizuda.snailjob.template.datasource.access.log.JobLogMessageAccess;
import com.aizuda.snailjob.template.datasource.access.log.RetryTaskLogMessageAccess;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.JobLogMessageDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.RetryTaskLogMessageDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfig {

    @ConditionalOnMissingBean
    @Bean
    public JobLogAccess<JobLogMessageDO> defaultJobLogAccess(JobLogMessageMapper jobLogMessageMapper, JobTaskBatchMapper jobTaskBatchMapper) {
        return new JobLogMessageAccess(jobLogMessageMapper, jobTaskBatchMapper);
    }

    @ConditionalOnMissingBean
    @Bean
    public RetryLogAccess<RetryTaskLogMessageDO> defaultRetryLogAccess(RetryTaskLogMessageMapper retryTaskLogMessageMapper) {
        return new RetryTaskLogMessageAccess(retryTaskLogMessageMapper);
    }
}
