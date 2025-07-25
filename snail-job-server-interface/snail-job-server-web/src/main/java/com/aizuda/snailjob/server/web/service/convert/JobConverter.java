package com.aizuda.snailjob.server.web.service.convert;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.util.TriggerIntervalUtils;
import com.aizuda.snailjob.server.web.model.request.JobRequestWebVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:40
 * @since : 2.4.0
 */
@Mapper
public interface JobConverter {

    JobConverter INSTANCE = Mappers.getMapper(JobConverter.class);

    List<JobRequestWebVO> convertList(List<Job> jobs);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIds(job.getNotifyIds()))")
    })
    JobRequestWebVO convert(Job job);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIdsStr(jobRequestWebVO.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(JobConverter.toTriggerInterval(jobRequestWebVO))")
    })
    Job convert(JobRequestWebVO jobRequestWebVO);

    static Set<Long> toNotifyIds(String notifyIds) {
        if (StrUtil.isBlank(notifyIds)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyIds, Long.class));
    }

    static String toNotifyIdsStr(Set<Long> notifyIds) {
        if (CollUtil.isEmpty(notifyIds)) {
            return StrUtil.EMPTY;
        }

        return JsonUtil.toJsonString(notifyIds);
    }

    static String toTriggerInterval(JobRequestWebVO jobRequestWebVO) {
        String triggerInterval = jobRequestWebVO.getTriggerInterval();
        if (StrUtil.isBlank(triggerInterval) || Objects.isNull(jobRequestWebVO.getTriggerType())) {
            return StrUtil.EMPTY;
        }

        return TriggerIntervalUtils.getPointInTimeStr(triggerInterval, jobRequestWebVO.getTriggerType());
    }
}
