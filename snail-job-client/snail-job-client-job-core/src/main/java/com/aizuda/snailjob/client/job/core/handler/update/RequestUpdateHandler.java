package com.aizuda.snailjob.client.job.core.handler.update;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.RequestUpdateJobDTO;
import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum;
import com.aizuda.snailjob.client.job.core.handler.AbstractRequestHandler;
import com.aizuda.snailjob.client.job.core.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.BlockStrategyEnum;
import com.aizuda.snailjob.common.core.enums.ExecutorTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobArgsTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum.CLUSTER;

public class RequestUpdateHandler extends AbstractRequestHandler<Boolean> {
    private final RequestUpdateJobDTO reqDTO;

    public RequestUpdateHandler(Long jobId) {
        this.reqDTO = new RequestUpdateJobDTO();
        // 更新必须要id
        reqDTO.setId(jobId);
        // 默认java
        reqDTO.setExecutorType(ExecutorTypeEnum.JAVA.getType());
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        return (Boolean) client.updateJob(reqDTO).getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        Pair<Boolean, String> validated = ValidatorUtils.validateEntity(reqDTO);
        if (!validated.getKey()) {
            return validated;
        }

        // 如果校验正确，下面则进行相关参数填充
        Optional.ofNullable(reqDTO.getTaskType()).ifPresent(taskType -> {
            if (reqDTO.getTaskType() == CLUSTER.getType()) {
                // 集群模式只允许并发为 1
                setParallelNum(1);
            } else {
                // 非集群模式 路由策略只能为轮询
                setRouteKey(AllocationAlgorithmEnum.ROUND);
            }
        });

        Optional.ofNullable(reqDTO.getTriggerType()).ifPresent((triggerType) -> {
            if (reqDTO.getTriggerType() == TriggerTypeEnum.WORK_FLOW.getType()) {
                // 工作流没有调度时间
                setTriggerInterval("*");
            }
        });

        return validated;
    }

    /**
     * 修改Reduce的分片数
     * 只允许MAP_REDUCE设置
     *
     * @param shardNum
     * @return
     */
    public RequestUpdateHandler setShardNum(Integer shardNum) {
        Integer taskType = reqDTO.getTaskType();
        if (taskType != null && taskType.equals(JobTaskTypeEnum.MAP_REDUCE.getType())) {
            // 设置分片
            if (shardNum != null) {
                Map<String, Object> map = new HashMap<>(1);
                map.put(SHARD_NUM, shardNum);
                reqDTO.setArgsStr(JsonUtil.toJsonString(map));
            }
        } else {
            throw new SnailJobClientException("非MapReduce模式不能设置分片数");
        }
        return this;
    }

    /**
     * 修改任务名称
     *
     * @param jobName
     * @return
     */
    public RequestUpdateHandler setJobName(String jobName) {
        reqDTO.setJobName(jobName);
        return this;
    }

    /**
     * 修改时会直接覆盖之前的任务参数
     * 修改参数
     *
     * @param argsStr
     * @return
     */
    private RequestUpdateHandler setArgsStr(Map<String, Object> argsStr) {
        Map<String, Object> args = new HashMap<>();
        if (StrUtil.isNotBlank(reqDTO.getArgsStr())) {
            args = JsonUtil.parseHashMap(reqDTO.getArgsStr());
        }
        args.putAll(argsStr);
        reqDTO.setArgsStr(JsonUtil.toJsonString(args));
        reqDTO.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        return this;
    }

    /**
     * 修改时会直接覆盖之前的任务参数
     * 添加参数，可支持多次添加
     * 静态分片不可使用该方法
     *
     * @param argsKey   参数名
     * @param argsValue 参数值
     * @return
     */
    public RequestUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        if (reqDTO.getTaskType() != null
                && reqDTO.getTaskType().equals(JobTaskTypeEnum.SHARDING.getType())) {
            SnailJobLog.LOCAL.warn("静态分片任务，不可使用该方法添加相关任务参数，请使用addShardingArgs");
            return this;
        }
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(reqDTO.getArgsStr())) {
            map = JsonUtil.parseHashMap(reqDTO.getArgsStr());
        }
        map.put(argsKey, argsValue);
        reqDTO.setArgsStr(JsonUtil.toJsonString(map));
        reqDTO.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        return this;
    }

    /**
     * 添加静态分片相关参数
     *
     * @param shardingValue
     * @return
     */
    public RequestUpdateHandler addShardingArgs(String[] shardingValue) {
        if (reqDTO.getTaskType() != null
                && !reqDTO.getTaskType().equals(JobTaskTypeEnum.SHARDING.getType())) {
            SnailJobLog.LOCAL.warn("非静态分片任务，不可使用该方法添加相关任务参数，请使用addArgsStr");
            return this;
        }
        reqDTO.setArgsStr(JsonUtil.toJsonString(shardingValue));
        reqDTO.setArgsType(JobArgsTypeEnum.TEXT.getArgsType());
        return this;
    }

    /**
     * 修改路由
     *
     * @param algorithmEnum
     * @return
     */
    public RequestUpdateHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        reqDTO.setRouteKey(algorithmEnum.getType());
        return this;
    }

    /**
     * 修改相关执行器
     *
     * @param executorInfo
     * @return
     */
    public RequestUpdateHandler setExecutorInfo(String executorInfo) {
        reqDTO.setExecutorInfo(executorInfo);
        return this;
    }

    /**
     * 修改调度类型
     *
     * @param triggerType
     * @return
     */
    public RequestUpdateHandler setTriggerType(TriggerTypeEnum triggerType) {
        reqDTO.setTriggerType(triggerType.getType());
        return this;
    }

    /**
     * 修改调度时间
     * 单位：秒
     * 工作流无需配置
     *
     * @param triggerInterval
     * @return
     */
    public RequestUpdateHandler setTriggerInterval(String triggerInterval) {
        reqDTO.setTriggerInterval(triggerInterval);
        return this;
    }

    /**
     * 修改阻塞策略
     *
     * @param blockStrategy
     * @return
     */
    public RequestUpdateHandler setBlockStrategy(BlockStrategyEnum blockStrategy) {
        reqDTO.setBlockStrategy(blockStrategy.getBlockStrategy());
        return this;
    }

    /**
     * 修改执行器超时时间
     *
     * @param executorTimeout
     * @return
     */
    public RequestUpdateHandler setExecutorTimeout(Integer executorTimeout) {
        reqDTO.setExecutorTimeout(executorTimeout);
        return this;
    }

    /**
     * 修改任务最大重试次数
     *
     * @param maxRetryTimes
     * @return
     */
    public RequestUpdateHandler setMaxRetryTimes(Integer maxRetryTimes) {
        reqDTO.setMaxRetryTimes(maxRetryTimes);
        return this;
    }

    /**
     * 修改重试间隔
     *
     * @param retryInterval
     * @return
     */
    public RequestUpdateHandler setRetryInterval(Integer retryInterval) {
        reqDTO.setRetryInterval(retryInterval);
        return this;
    }

    /**
     * 修改并发数量
     *
     * @param parallelNum
     * @return
     */
    public RequestUpdateHandler setParallelNum(Integer parallelNum) {
        reqDTO.setParallelNum(parallelNum);
        return this;
    }

    /**
     * 修改定时任务描述
     *
     * @param description
     * @return
     */
    public RequestUpdateHandler setDescription(String description) {
        reqDTO.setDescription(description);
        return this;
    }


}
