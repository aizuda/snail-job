package com.aizuda.snailjob.server.job.task.support.convert;

import com.aizuda.snailjob.model.request.JobExecutorRequest;
import com.aizuda.snailjob.template.datasource.persistence.po.JobExecutor;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author：srzou
 * @Project：snail-job
 * @since 1.6.0
 */
@Mapper
public interface JobExecutorConverter {

    JobExecutorConverter INSTANCE = Mappers.getMapper(JobExecutorConverter.class);


    List<JobExecutor> toJobExecutor(List<JobExecutorRequest> jobExecutorRequests);

}
