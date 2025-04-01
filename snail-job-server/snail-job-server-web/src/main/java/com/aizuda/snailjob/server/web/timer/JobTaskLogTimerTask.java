package com.aizuda.snailjob.server.web.timer;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.common.enums.WebSocketSceneEnum;
import com.aizuda.snailjob.server.common.vo.JobLogQueryVO;
import com.aizuda.snailjob.server.web.service.JobLogService;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.time.LocalDateTime;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.common.timer
 * @Project：snail-job
 * @Date：2025/3/24 14:08
 * @Filename：JobTaskLogTimerTask
 * @since 1.5.0
 */
@AllArgsConstructor
public class JobTaskLogTimerTask implements TimerTask<String> {
    private static final String IDEMPOTENT_KEY_PREFIX = "jobTaskLog_{0}_{1}_{2}";
    private JobLogQueryVO logQueryVO;
    private String sid;

    @Override
    public void run(final Timeout timeout) throws Exception {
        SnailJobLog.LOCAL.debug("开始执行定时任务日志查询. 当前时间:[{}] jobTaskId:[{}]", LocalDateTime.now(), logQueryVO.getTaskBatchId());

        try {
            LogTimerWheel.clearCache(idempotentKey());
            JobLogService logService = SnailSpringContext.getBean(JobLogService.class);
            logService.getJobLogPageV2(logQueryVO);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("定时任务日志查询执行失败", e);
        }
    }

    @Override
    public String idempotentKey() {

        Long jobTaskId = logQueryVO.getTaskBatchId();
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, sid, WebSocketSceneEnum.JOB_LOG_SCENE, jobTaskId);
    }
}
