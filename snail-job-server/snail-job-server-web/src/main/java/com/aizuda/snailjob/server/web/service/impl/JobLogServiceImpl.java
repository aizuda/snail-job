package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.server.common.vo.JobLogQueryVO;
import com.aizuda.snailjob.server.common.vo.JobLogResponseVO;
import com.aizuda.snailjob.server.web.service.JobLogService;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum.COMPLETED;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:55
 * @since ：2.4.0
 */
@Service
@RequiredArgsConstructor
public class JobLogServiceImpl implements JobLogService {
    private final JobLogMessageMapper jobLogMessageMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;

    @Override
    public JobLogResponseVO getJobLogPage(final JobLogQueryVO queryVO) {

        PageDTO<JobLogMessage> pageDTO = new PageDTO<>(1, queryVO.getSize());

        PageDTO<JobLogMessage> selectPage = jobLogMessageMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<JobLogMessage>()
                        .select(JobLogMessage::getId, JobLogMessage::getLogNum)
                        .ge(JobLogMessage::getId, queryVO.getStartId())
                        .eq(JobLogMessage::getTaskBatchId, queryVO.getTaskBatchId())
//                        .ge(JobLogMessage::getJobId, queryVO.getJobId())
                        .eq(JobLogMessage::getTaskId, queryVO.getTaskId())
                        .orderByAsc(JobLogMessage::getId).orderByAsc(JobLogMessage::getRealTime));
        List<JobLogMessage> records = selectPage.getRecords();
        if (CollUtil.isEmpty(records)) {

            JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectOne(
                    new LambdaQueryWrapper<JobTaskBatch>()
                            .eq(JobTaskBatch::getId, queryVO.getTaskBatchId())
            );

            JobLogResponseVO jobLogResponseVO = new JobLogResponseVO();

            if (Objects.isNull(jobTaskBatch)
                    ||  (COMPLETED.contains(jobTaskBatch.getTaskBatchStatus()) &&
            jobTaskBatch.getUpdateDt().plusSeconds(15).isBefore(LocalDateTime.now()))

            ) {
                jobLogResponseVO.setFinished(Boolean.TRUE);
            }

            jobLogResponseVO.setNextStartId(queryVO.getStartId());
            jobLogResponseVO.setFromIndex(0);
            return jobLogResponseVO;
        }

        Integer fromIndex = Optional.ofNullable(queryVO.getFromIndex()).orElse(0);
        JobLogMessage firstRecord = records.get(0);
        List<Long> ids = Lists.newArrayList(firstRecord.getId());
        int total = firstRecord.getLogNum() - fromIndex;
        for (int i = 1; i < records.size(); i++) {
            JobLogMessage record = records.get(i);
            if (total + record.getLogNum() > queryVO.getSize()) {
                break;
            }

            total += record.getLogNum();
            ids.add(record.getId());
        }

        long nextStartId = 0;
        List<Map<String, String>> messages = Lists.newArrayList();
        List<JobLogMessage> jobLogMessages = jobLogMessageMapper.selectList(
                new LambdaQueryWrapper<JobLogMessage>()
                        .in(JobLogMessage::getId, ids)
                        .orderByAsc(JobLogMessage::getId)
                        .orderByAsc(JobLogMessage::getRealTime)
        );

        for (final JobLogMessage jobLogMessage : jobLogMessages) {

            List<Map<String, String>> originalList = JsonUtil.parseObject(jobLogMessage.getMessage(), List.class);
            int size = originalList.size() - fromIndex;
            List<Map<String, String>> pageList = originalList.stream().skip(fromIndex).limit(queryVO.getSize())
                    .collect(Collectors.toList());

            if (messages.size() + size >= queryVO.getSize()) {
                messages.addAll(pageList);
                nextStartId = jobLogMessage.getId();
                fromIndex = Math.min(fromIndex + queryVO.getSize(), originalList.size() - 1) + 1;
                break;
            }

            messages.addAll(pageList);
            nextStartId = jobLogMessage.getId() + 1;
            fromIndex = 0;
        }

        messages = messages.stream()
                .sorted(Comparator.comparingLong(o -> Long.parseLong(o.get(LogFieldConstants.TIME_STAMP))))
                .collect(Collectors.toList());

        JobLogResponseVO jobLogResponseVO = new JobLogResponseVO();
        jobLogResponseVO.setMessage(messages);
        jobLogResponseVO.setNextStartId(nextStartId);
        jobLogResponseVO.setFromIndex(fromIndex);
        return jobLogResponseVO;
    }
}
