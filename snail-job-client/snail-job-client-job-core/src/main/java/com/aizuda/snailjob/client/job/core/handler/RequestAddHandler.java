package com.aizuda.snailjob.client.job.core.handler;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.RequestAddJobDTO;
import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum;
import com.aizuda.snailjob.client.job.core.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.*;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;

import java.util.HashMap;
import java.util.Map;

public class RequestAddHandler extends AbstractRequestHandler<Long> {
    private final RequestAddJobDTO requestAddJobDTO;

    public RequestAddHandler(JobTaskTypeEnum taskType, Integer shardNum) {
        this.requestAddJobDTO = new RequestAddJobDTO();
        // 默认创建就开启
        requestAddJobDTO.setJobStatus(StatusEnum.YES.getStatus());
        // 设置任务类型
        requestAddJobDTO.setTaskType(taskType.getType());
        // 默认java
        requestAddJobDTO.setExecutorType(ExecutorTypeEnum.JAVA.getType());
        // 设置分片
        if (shardNum != null) {
            Map<String, Object> map = new HashMap<>(1);
            map.put(SHARD_NUM, shardNum);
            requestAddJobDTO.setArgsStr(JsonUtil.toJsonString(map));
        }
    }


    @Override
    protected Long doExecute() {
        String data = JsonUtil.toJsonString(client.addJob(requestAddJobDTO).getData());
        return Long.valueOf(data);
    }

    @Override
    protected boolean checkRequest() {
        boolean validated = ValidatorUtils.validateEntity(requestAddJobDTO);
        // 如果校验正确，则正对进行相关筛选
        if (validated) {
            if (requestAddJobDTO.getTaskType() == JobTaskTypeEnum.CLUSTER.getType()) {
                // 集群模式只允许并发为 1
                setParallelNum(1);
            }
            if (requestAddJobDTO.getTriggerType() == TriggerTypeEnum.WORK_FLOW.getType()) {
                // 工作流没有调度时间
                setTriggerInterval("*");
            }
        }
        return validated;
    }

    /**
     * 设置任务名
     *
     * @param jobName 任务名
     * @return
     */
    public RequestAddHandler setJobName(String jobName) {
        requestAddJobDTO.setJobName(jobName);
        return this;
    }

    /**
     * 设置参数
     *
     * @param argsStr
     * @return
     */
    private RequestAddHandler setArgsStr(Map<String, Object> argsStr) {
        Map<String, Object> args = new HashMap<>();
        if (StrUtil.isNotBlank(requestAddJobDTO.getArgsStr())) {
            args = JsonUtil.parseHashMap(requestAddJobDTO.getArgsStr());
        }
        args.putAll(argsStr);
        requestAddJobDTO.setArgsStr(JsonUtil.toJsonString(args));
        requestAddJobDTO.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        return this;
    }

    /**
     * 添加参数，可支持多次添加
     * 静态分片不可使用该方法
     *
     * @param argsKey   参数名
     * @param argsValue 参数值
     * @return
     */
    public RequestAddHandler addArgsStr(String argsKey, Object argsValue) {
        if (requestAddJobDTO.getTaskType().equals(JobTaskTypeEnum.SHARDING.getType())) {
            SnailJobLog.LOCAL.warn("静态分片任务，不可使用该方法添加相关任务参数，请使用addShardingArgs");
            return this;
        }
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(requestAddJobDTO.getArgsStr())) {
            map = JsonUtil.parseHashMap(requestAddJobDTO.getArgsStr());
        }
        map.put(argsKey, argsValue);
        requestAddJobDTO.setArgsStr(JsonUtil.toJsonString(map));
        requestAddJobDTO.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        return this;
    }

    /**
     * 添加静态分片相关参数
     *
     * @param shardingValue
     * @return
     */
    public RequestAddHandler addShardingArgs(String[] shardingValue) {
        if (!requestAddJobDTO.getTaskType().equals(JobTaskTypeEnum.SHARDING.getType())) {
            SnailJobLog.LOCAL.warn("非静态分片任务，不可使用该方法添加相关任务参数，请使用addArgsStr");
            return this;
        }
        requestAddJobDTO.setArgsStr(JsonUtil.toJsonString(shardingValue));
        requestAddJobDTO.setArgsType(JobArgsTypeEnum.TEXT.getArgsType());
        return this;
    }

    /**
     * 设置路由
     *
     * @param algorithmEnum 路由算法
     * @return
     */
    public RequestAddHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        // 非集群模式 路由策略只能为轮询
        if (requestAddJobDTO.getTaskType() != JobTaskTypeEnum.CLUSTER.getType()) {
            setRouteKey(AllocationAlgorithmEnum.ROUND);
            SnailJobLog.LOCAL.warn("非集群模式 路由策略只能为轮询");
            return this;
        }
        requestAddJobDTO.setRouteKey(algorithmEnum.getType());
        return this;
    }

    /**
     * 设置执行器信息
     *
     * @param executorInfo
     * @return
     */
    public RequestAddHandler setExecutorInfo(String executorInfo) {
        requestAddJobDTO.setExecutorInfo(executorInfo);
        return this;
    }

    /**
     * 设置调度类型
     *
     * @param triggerType
     * @return
     */
    public RequestAddHandler setTriggerType(TriggerTypeEnum triggerType) {
        requestAddJobDTO.setTriggerType(triggerType.getType());
        if (requestAddJobDTO.getTriggerType() == TriggerTypeEnum.WORK_FLOW.getType()) {
            // 工作流没有调度时间
            setTriggerInterval("*");
        }
        return this;
    }

    /**
     * 设置触发间隔；
     * 单位：秒
     * 工作流无需配置
     *
     * @param triggerInterval
     * @return
     */
    public RequestAddHandler setTriggerInterval(String triggerInterval) {
        requestAddJobDTO.setTriggerInterval(triggerInterval);
        return this;
    }

    /**
     * 设置阻塞策略
     *
     * @param blockStrategy
     * @return
     */
    public RequestAddHandler setBlockStrategy(BlockStrategyEnum blockStrategy) {
        // 非集群模式 路由策略只能为轮询
        if (requestAddJobDTO.getTaskType() == JobTaskTypeEnum.CLUSTER.getType()
                && blockStrategy.getBlockStrategy() == BlockStrategyEnum.CONCURRENCY.getBlockStrategy()) {
            throw new SnailJobClientException("集群模式不能使用并行阻塞策略");
        }
        requestAddJobDTO.setBlockStrategy(blockStrategy.getBlockStrategy());
        return this;
    }

    /**
     * 设置执行器超时时间
     *
     * @param executorTimeout
     * @return
     */
    public RequestAddHandler setExecutorTimeout(Integer executorTimeout) {
        requestAddJobDTO.setExecutorTimeout(executorTimeout);
        return this;
    }

    /**
     * 设置任务最大重试次数
     *
     * @param maxRetryTimes
     * @return
     */
    public RequestAddHandler setMaxRetryTimes(Integer maxRetryTimes) {
        requestAddJobDTO.setMaxRetryTimes(maxRetryTimes);
        return this;
    }

    /**
     * 设置重试间隔
     *
     * @param retryInterval
     * @return
     */
    public RequestAddHandler setRetryInterval(Integer retryInterval) {
        requestAddJobDTO.setRetryInterval(retryInterval);
        return this;
    }

    /**
     * 设置并发数量
     *
     * @param parallelNum
     * @return
     */
    public RequestAddHandler setParallelNum(Integer parallelNum) {
        requestAddJobDTO.setParallelNum(parallelNum);
        return this;
    }

    /**
     * 设置定时任务描述
     *
     * @param description
     * @return
     */
    public RequestAddHandler setDescription(String description) {
        requestAddJobDTO.setDescription(description);
        return this;
    }

}
