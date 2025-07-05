package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.web.model.response.DashboardCardResponseVO.JobTask;
import com.aizuda.snailjob.server.web.model.response.DashboardCardResponseVO.WorkFlowTask;
import com.aizuda.snailjob.server.web.model.response.DashboardRetryLineResponseVO.Task;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardCardResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * @author zhengweilin
 * @version 2.6.0
 * @date 2023/11/24
 */
@Mapper
public interface JobSummaryResponseVOConverter {

    JobSummaryResponseVOConverter INSTANCE = Mappers.getMapper(JobSummaryResponseVOConverter.class);

    @Mappings({
            @Mapping(target = "successRate", expression = "java(JobSummaryResponseVOConverter.toSuccessRate(jobTask.getSuccessNum(), jobTask.getTotalNum()))")
    })
    JobTask convert(DashboardCardResponseDO.JobTask jobTask);

    @Mappings({
            @Mapping(target = "successRate", expression = "java(JobSummaryResponseVOConverter.toSuccessRate(jobTask.getSuccessNum(), jobTask.getTotalNum()))")
    })
    WorkFlowTask convertToWorkFlowTask(DashboardCardResponseDO.JobTask jobTask);

    List<Task> convertList(List<DashboardRetryLineResponseDO.Task> taskList);

    static BigDecimal toSuccessRate(Integer successNum, Integer totalNum) {
        if (Objects.isNull(totalNum) || totalNum == 0) {
            return null;
        }
        return new BigDecimal(String.valueOf(successNum)).divide(new BigDecimal(String.valueOf(totalNum)), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }
}
