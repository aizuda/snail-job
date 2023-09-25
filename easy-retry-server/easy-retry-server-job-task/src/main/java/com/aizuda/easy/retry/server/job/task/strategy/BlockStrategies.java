package com.aizuda.easy.retry.server.job.task.strategy;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.scan.JobContext;
import com.aizuda.easy.retry.server.job.task.scan.JobTimerTask;
import com.aizuda.easy.retry.server.job.task.scan.JobTimerWheelHandler;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

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
            Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new EasyRetryServerException("新增调度任务失败.jobId:[{}]", job.getId()));

            JobContext jobContext = new JobContext();
            // 进入时间轮
            JobTimerWheelHandler.register(job.getGroupName(), job.getId().toString(), new JobTimerTask(jobContext), 1, TimeUnit.MILLISECONDS);

            return false;
        }
    }


}
