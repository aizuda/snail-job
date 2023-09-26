package com.aizuda.easy.retry.server.job.task.strategy;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.model.InterruptJobDTO;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.client.RequestBuilder;
import com.aizuda.easy.retry.server.common.client.RpcClient;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.enums.TaskStatusEnum;
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

            throw new EasyRetryServerException("不符合的阻塞策略. blockStrategy:[{}]", blockStrategy);
        }

    }

    @Data
    public static class BlockStrategyContext {

        private Long jobId;

        private Long taskId;

        private String groupName;

        private RegisterNodeInfo registerNodeInfo;
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
            RegisterNodeInfo registerNodeInfo = context.registerNodeInfo;
            RpcClient rpcClient = RequestBuilder.<RpcClient, Result>newBuilder()
                .hostPort(registerNodeInfo.getHostPort())
                .groupName(registerNodeInfo.getGroupName())
                .hostId(registerNodeInfo.getHostId())
                .hostIp(registerNodeInfo.getHostIp())
                .contextPath(registerNodeInfo.getContextPath())
                .client(RpcClient.class)
                .build();

            InterruptJobDTO interruptJobDTO = new InterruptJobDTO();
            interruptJobDTO.setTaskId(context.getTaskId());
            interruptJobDTO.setGroupName(context.getGroupName());
            interruptJobDTO.setJobId(context.getJobId());

            // TODO 处理结果
            Result<Boolean> result = rpcClient.interrupt(interruptJobDTO);
            Integer taskStatus;
            if (result.getStatus() == StatusEnum.YES.getStatus() && Boolean.TRUE.equals(result.getData())) {
                taskStatus = TaskStatusEnum.INTERRUPT_SUCCESS.getStatus();

                // 生成一个新的任务
                JobTask jobTask = new JobTask();
                jobTask.setJobId(context.getJobId());
                jobTask.setGroupName(context.getGroupName());
                JobTaskMapper jobTaskMapper = SpringContext.getBeanByType(JobTaskMapper.class);
                Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new EasyRetryServerException("新增调度任务失败.jobId:[{}]", context.getJobId()));

                JobContext jobContext = new JobContext();
                // 进入时间轮
                JobTimerWheelHandler.register(context.getGroupName(), context.getJobId().toString(), new JobTimerTask(jobTask.getJobId(), jobTask.getGroupName()), 1, TimeUnit.MILLISECONDS);

            } else {
                taskStatus = TaskStatusEnum.INTERRUPT_FAIL.getStatus();
            }

            JobTaskMapper jobTaskMapper = SpringContext.getBeanByType(JobTaskMapper.class);
            JobTask jobTask = new JobTask();
            jobTask.setTaskStatus(taskStatus);
            Assert.isTrue(1 == jobTaskMapper.updateById(jobTask), ()-> new EasyRetryServerException("更新调度任务失败. jopId:[{}]", context.getJobId()));

            return true;
        }
    }

    private static final class ConcurrencyBlockStrategy implements BlockStrategy {

        @Override
        public boolean block(final BlockStrategyContext context) {
            log.warn("阻塞策略为并行执行. jobId:[{}]", context.getJobId());

            JobTask jobTask = new JobTask();
            jobTask.setJobId(context.getJobId());
            jobTask.setGroupName(context.getGroupName());
            JobTaskMapper jobTaskMapper = SpringContext.getBeanByType(JobTaskMapper.class);
            Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new EasyRetryServerException("新增调度任务失败.jobId:[{}]", context.getJobId()));

            // 进入时间轮
            JobTimerWheelHandler.register(context.getGroupName(), context.getJobId().toString(), new JobTimerTask(jobTask.getJobId(), jobTask.getGroupName()), 1, TimeUnit.MILLISECONDS);

            return false;
        }
    }


}
