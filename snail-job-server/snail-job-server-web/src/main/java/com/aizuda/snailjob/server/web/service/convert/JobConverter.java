package com.aizuda.snailjob.server.web.service.convert;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.web.model.request.JobRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:40
 * @since : 2.4.0
 */
@Mapper
public interface JobConverter {

    JobConverter INSTANCE = Mappers.getMapper(JobConverter.class);

    List<JobRequestVO> convertList(List<Job> jobs);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIds(job.getNotifyIds()))")
    })
    JobRequestVO convert(Job job);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIdsStr(jobRequestVO.getNotifyIds()))")
    })
    Job convert(JobRequestVO jobRequestVO);

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
}
