package com.aizuda.snailjob.server.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.common.log.dto.TaskLogFieldDTO;
import com.aizuda.snailjob.server.common.dto.JobLogDTO;
import com.aizuda.snailjob.server.common.service.LogService;
import com.aizuda.snailjob.server.common.convert.JobLogMessageConverter;
import com.aizuda.snailjob.server.common.vo.JobLogQueryVO;
import com.aizuda.snailjob.server.common.vo.JobLogResponseVO;
import com.aizuda.snailjob.server.model.dto.JobLogTaskDTO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum.COMPLETED;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.common.service.impl
 * @Project：snail-job
 * @Date：2025/3/10 21:12
 * @Filename：DatabaseLogService
 * @since 1.5.0
 */

@Slf4j
@RequiredArgsConstructor
public class DatabaseLogService implements LogService {
    private final JobLogMessageMapper jobLogMessageMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    // 创建一个调度线程池
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 保存单调日志
     *
     * @param jobLogDTO
     */
    @Override
    public void saveLog(JobLogDTO jobLogDTO) {
        JobLogMessage jobLogMessage = JobLogMessageConverter.INSTANCE.toJobLogMessage(jobLogDTO);
        jobLogMessage.setCreateDt(LocalDateTime.now());
        jobLogMessage.setLogNum(1);
        jobLogMessage.setMessage(Optional.ofNullable(jobLogDTO.getMessage()).orElse(StrUtil.EMPTY));
        jobLogMessage.setTaskId(Optional.ofNullable(jobLogMessage.getTaskId()).orElse(0L));
        jobLogMessageMapper.insert(jobLogMessage);
    }

    /**
     * 批量保存日志
     *
     * @param jobLogTasks
     */
    @Override
    public void batchSaveLogs(List<JobLogTaskDTO> jobLogTasks) {
        Map<Long, List<JobLogTaskDTO>> logTaskDTOMap = jobLogTasks.
                stream().collect(Collectors.groupingBy(JobLogTaskDTO::getTaskId, Collectors.toList()));

        List<JobLogMessage> jobLogMessageList = new ArrayList<>();
        for (List<JobLogTaskDTO> logTaskDTOList : logTaskDTOMap.values()) {
            JobLogMessage jobLogMessage = JobLogMessageConverter.INSTANCE.toJobLogMessage(logTaskDTOList.get(0));
            jobLogMessage.setCreateDt(LocalDateTime.now());
            jobLogMessage.setLogNum(logTaskDTOList.size());
            List<Map<String, String>> messageMapList = StreamUtils.toList(logTaskDTOList,
                    taskDTO -> taskDTO.getFieldList()
                            .stream().filter(logTaskDTO_ -> !Objects.isNull(logTaskDTO_.getValue()))
                            .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue)));
            jobLogMessage.setMessage(JsonUtil.toJsonString(messageMapList));

            jobLogMessageList.add(jobLogMessage);
        }

        jobLogMessageMapper.insertBatch(jobLogMessageList);
    }

    @Override
    public void getJobLogPage(JobLogQueryVO queryVO, Session session) throws IOException {
        Boolean taskBatchComplete = false;
        while (!taskBatchComplete){
            PageDTO<JobLogMessage> pageDTO = new PageDTO<>(1, queryVO.getSize());

            PageDTO<JobLogMessage> selectPage = jobLogMessageMapper.selectPage(pageDTO,
                    new LambdaQueryWrapper<JobLogMessage>()
                            .select(JobLogMessage::getId, JobLogMessage::getLogNum)
                            .ge(JobLogMessage::getId, queryVO.getStartId())
                            .eq(JobLogMessage::getTaskBatchId, queryVO.getTaskBatchId())
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
                    jobLogResponseVO.setNextStartId(queryVO.getStartId());
                    jobLogResponseVO.setFromIndex(0);
                    session.getBasicRemote().sendText(JsonUtil.toJsonString(jobLogResponseVO));
                    System.out.println("结束了");
                    return;
                }else {
                    // 如果没有完成，就等五秒执行
                    System.out.println("异步执行");
                    scheduleNextAttempt(queryVO, session);
                    return;
                }
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
            session.getBasicRemote().sendText(JsonUtil.toJsonString(jobLogResponseVO));

            queryVO.setFromIndex(fromIndex);
            queryVO.setStartId(nextStartId);
        }

    }

    private void scheduleNextAttempt(JobLogQueryVO queryVO, Session session) {
        scheduler.schedule(() -> {
            try {
                // 再次调用查询
                getJobLogPage(queryVO, session);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 5秒后执行
        }, 5, TimeUnit.SECONDS);
    }
}
