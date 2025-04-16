package com.aizuda.snailjob.server.web.service.impl;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.server.web.timer.JobTaskLogTimerTask;
import com.aizuda.snailjob.server.web.timer.LogTimerWheel;
import com.aizuda.snailjob.server.common.vo.JobLogQueryVO;
import com.aizuda.snailjob.server.web.model.event.WsSendEvent;
import com.aizuda.snailjob.server.web.service.JobLogService;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.common.PageResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.JobLogMessageDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.LogPageQueryDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum.COMPLETED;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:55
 * @since ：2.4.0
 */
@Service
@RequiredArgsConstructor
public class JobLogServiceImpl implements JobLogService {
    private static final Long DELAY_MILLS = 5000L;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final AccessTemplate accessTemplate;

    @Override
    public void getJobLogPage(JobLogQueryVO queryVO) {
        String sid = queryVO.getSid();
        LogPageQueryDO pageQueryDO = new LogPageQueryDO();
        pageQueryDO.setPage(1);
        pageQueryDO.setSize(queryVO.getSize());
        pageQueryDO.setTaskBatchId(queryVO.getTaskBatchId());
        pageQueryDO.setTaskId(queryVO.getTaskId());
        pageQueryDO.setStartRealTime(queryVO.getStartRealTime());
        pageQueryDO.setSearchCount(true);
        PageResponseDO<JobLogMessageDO> pageResponseDO = accessTemplate.getJobLogMessageAccess()
                .listPage(pageQueryDO);

        long total = pageResponseDO.getTotal();

        int totalPage = (int) ((total + queryVO.getSize() - 1) / queryVO.getSize());

        Long lastRealTime = null;
        for (int i = 1; i < totalPage; i++) {
            for (JobLogMessageDO jobLogMessageDO : pageResponseDO.getRows()) {
                // 循环覆盖，最后一个肯定是最大的
                lastRealTime = jobLogMessageDO.getRealTime();
                // 发生日志内容到前端
                String message = jobLogMessageDO.getMessage();
                List<Map<String, String>> logContents = JsonUtil.parseObject(message, List.class);
                logContents = logContents.stream()
                        .sorted(Comparator.comparingLong(o -> Long.parseLong(o.get(LogFieldConstants.TIME_STAMP))))
                        .toList();
                for (Map<String, String> logContent : logContents) {
                    // send发消息
                    WsSendEvent sendEvent = new WsSendEvent(this);
                    sendEvent.setSid(sid);
                    sendEvent.setMessage(JsonUtil.toJsonString(logContent));
                    SnailSpringContext.getContext().publishEvent(sendEvent);
                }
            }

            // 继续查询下一页
            pageQueryDO.setSearchCount(false);
            pageQueryDO.setPage((i - 1) * queryVO.getSize());
            pageResponseDO = accessTemplate.getJobLogMessageAccess()
                    .listPage(pageQueryDO);
        }


        // 这里判断是否继续查询
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectOne(
                new LambdaQueryWrapper<JobTaskBatch>().eq(JobTaskBatch::getId, queryVO.getTaskBatchId()));

        // 结束查询
        if (Objects.isNull(jobTaskBatch)
                || (COMPLETED.contains(jobTaskBatch.getTaskBatchStatus()) &&
                jobTaskBatch.getUpdateDt().plusSeconds(15).isBefore(LocalDateTime.now()))) {
            // 发生完成标识
            WsSendEvent sendEvent = new WsSendEvent(this);
            sendEvent.setMessage("END");
            sendEvent.setSid(sid);
            SnailSpringContext.getContext().publishEvent(sendEvent);
        } else {
            // 覆盖作为下次查询的起始条件
            pageQueryDO.setStartRealTime(lastRealTime);
            // 继续查询
            scheduleNextAttempt(queryVO, sid);
        }


    }

    /**
     * 使用时间轮5秒再进行日志查询
     *
     * @param queryVO
     * @param sid
     */
    private void scheduleNextAttempt(JobLogQueryVO queryVO, String sid) {
        LogTimerWheel.registerWithJobTaskLog(() -> new JobTaskLogTimerTask(queryVO, sid), Duration.ofMillis(DELAY_MILLS));
    }
}
