package com.aizuda.snailjob.server.common.service;

import com.aizuda.snailjob.server.common.dto.JobLogDTO;
import com.aizuda.snailjob.server.common.vo.JobLogQueryVO;
import com.aizuda.snailjob.server.model.dto.JobLogTaskDTO;

import java.io.IOException;
import java.util.List;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.common.service
 * @Project：snail-job
 * @Date：2025/3/10 20:57
 * @Filename：LogService
 * @since 1.5.0
 */
public interface LogService {
    void saveLog(JobLogDTO jobLogDTO);
    void batchSaveLogs(List<JobLogTaskDTO> jobLogTasks);
    void getJobLogPage(JobLogQueryVO queryVO, String sid) throws IOException;
}
