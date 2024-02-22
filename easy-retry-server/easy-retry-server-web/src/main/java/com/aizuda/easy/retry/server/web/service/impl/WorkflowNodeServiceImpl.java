package com.aizuda.easy.retry.server.web.service.impl;

import com.aizuda.easy.retry.server.web.service.WorkflowNodeService;
import org.springframework.stereotype.Service;

/**
 * @author xiaowoniu
 * @date 2024-02-03 21:25:00
 * @since 2.6.0
 */
@Service
public class WorkflowNodeServiceImpl implements WorkflowNodeService {

    @Override
    public Boolean stop(Long id) {
        // 调用JOB的停止接口
        // 继续执行后续的任务
        return null;
    }

    @Override
    public Boolean retry(Long id) {
        return null;
    }
}
