package com.aizuda.easy.retry.server.job.task.strategy;

import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.server.job.task.BlockStrategy;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:52
 */
@Slf4j
public class BlockStrategies {

    @AllArgsConstructor
    @Getter
    public enum BlockStrategyEnum {
        DISCARD(1, new DiscardBlockStrategy()),
        OVERLAY(2, new OverlayBlockStrategy()),
        CONCURRENCY(3, new ConcurrencyBlockStrategy());

        private final int blockStrategy;
        private final BlockStrategy strategy;

        public static BlockStrategy getBlockStrategy(int blockStrategy) {
            for (final BlockStrategyEnum value : BlockStrategyEnum.values()) {
                if (value.blockStrategy == blockStrategy) {
                    return value.getStrategy();
                }
            }

            return null;
        }


    }

    @Data
    public static class BlockStrategyContext {

        private Long jobId;

        private Job job;
    }

    private static final class DiscardBlockStrategy implements BlockStrategy {

        @Override
        public boolean block(final BlockStrategyContext context) {
            log.warn("阻塞策略为丢弃此次执行. jobId:[{}]", context.getJobId());
            return false;
        }
    }

    private static final class OverlayBlockStrategy implements BlockStrategy {

        @Override
        public boolean block(final BlockStrategyContext context) {
            log.warn("阻塞策略为覆盖. jobId:[{}]", context.getJobId());
            // 向客户端发送中断执行指令

            return true;
        }
    }

    private static final class ConcurrencyBlockStrategy implements BlockStrategy {

        @Override
        public boolean block(final BlockStrategyContext context) {
            log.warn("阻塞策略为并行执行. jobId:[{}]", context.getJobId());
            Job job = context.getJob();

            JobTask jobTask = new JobTask();
            jobTask.setJobId(job.getId());
            jobTask.setGroupName(job.getGroupName());

            JobTaskMapper jobTaskMapper = SpringContext.getBeanByType(JobTaskMapper.class);
            jobTaskMapper.insert(jobTask);

            return false;
        }
    }


}
