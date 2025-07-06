package com.aizuda.snailjob.server.job.task.support;

/**
 * @author: xiaowoniu
 * @date : 2024-01-02
 * @since : 2.6.0
 */
@FunctionalInterface
public interface LockExecutor {
    void execute();
}
