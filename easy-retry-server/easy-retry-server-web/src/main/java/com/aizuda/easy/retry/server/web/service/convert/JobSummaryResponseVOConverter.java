package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.DashboardRetryLineResponseVO;
import com.aizuda.easy.retry.server.web.model.response.DashboardCardResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardRetryLineResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.DashboardCardResponseDO;
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
    DashboardCardResponseVO.JobTask toTaskJob(DashboardCardResponseDO.JobTask jobTask);

    @Mappings({
            @Mapping(target = "successRate", expression = "java(JobSummaryResponseVOConverter.toSuccessRate(jobTask.getSuccessNum(), jobTask.getTotalNum()))")
    })
    DashboardCardResponseVO.WorkFlowTask toWorkFlowTask(DashboardCardResponseDO.JobTask jobTask);

    List<DashboardRetryLineResponseVO.Task> toDashboardRetryLineResponseVO(List<DashboardRetryLineResponseDO.Task> taskList);

    static BigDecimal toSuccessRate(Integer successNum, Integer totalNum) {
        if (Objects.isNull(totalNum) || totalNum == 0) {
            return null;
        }
        return new BigDecimal(String.valueOf(successNum)).divide(new BigDecimal(String.valueOf(totalNum)), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }
}
