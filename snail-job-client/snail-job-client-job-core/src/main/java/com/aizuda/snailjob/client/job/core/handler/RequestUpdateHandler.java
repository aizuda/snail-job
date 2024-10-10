package com.aizuda.snailjob.client.job.core.handler;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.RequestUpdateJobDTO;
import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum;
import com.aizuda.snailjob.client.job.core.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.BlockStrategyEnum;
import com.aizuda.snailjob.common.core.enums.ExecutorTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobArgsTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;

import java.util.HashMap;
import java.util.Map;

public class RequestUpdateHandler extends AbstractRequestHandler<Boolean> {
    private final RequestUpdateJobDTO requestUpdateJobDTO;

    public RequestUpdateHandler(Long jobId) {
        this.requestUpdateJobDTO = new RequestUpdateJobDTO();
        // 更新必须要id
        requestUpdateJobDTO.setId(jobId);
        // 默认java
        requestUpdateJobDTO.setExecutorType(ExecutorTypeEnum.JAVA.getType());
    }

    @Override
    protected Boolean doExecute() {
        return (Boolean) client.updateJob(requestUpdateJobDTO).getData();
    }

    @Override
    protected boolean checkRequest() {
        boolean validated = ValidatorUtils.validateEntity(requestUpdateJobDTO);
        // 如果校验正确，则正对进行相关筛选
        if (validated) {
            if (requestUpdateJobDTO.getTaskType() != null
                    && requestUpdateJobDTO.getTaskType() == JobTaskTypeEnum.CLUSTER.getType()){
                // 集群模式只允许并发为 1
                setParallelNum(1);
            }
            // 非集群模式 路由策略只能为轮询
            if (requestUpdateJobDTO.getTaskType() != null
                    && requestUpdateJobDTO.getTaskType() != JobTaskTypeEnum.CLUSTER.getType()){
                setRouteKey(AllocationAlgorithmEnum.ROUND);
            }
            if (requestUpdateJobDTO.getTriggerType() != null
                    && requestUpdateJobDTO.getTriggerType() == TriggerTypeEnum.WORK_FLOW.getType()){
                // 工作流没有调度时间
                setTriggerInterval("*");
            }
        }
        return validated;
    }

    /**
     * 修改Reduce的分片数
     * 只允许MAP_REDUCE设置
     * @param shardNum
     * @return
     */
    public RequestUpdateHandler setShardNum(Integer shardNum){
        Integer taskType = requestUpdateJobDTO.getTaskType();
        if (taskType != null && taskType.equals(JobTaskTypeEnum.MAP_REDUCE.getType())){
            // 设置分片
            if (shardNum != null){
                Map<String, Object> map = new HashMap<>(1);
                map.put(SHARD_NUM, shardNum);
                requestUpdateJobDTO.setArgsStr(JsonUtil.toJsonString(map));
            }
        }else {
            throw new SnailJobClientException("非MapReduce模式不能设置分片数");
        }
        return this;
    }

    /**
     * 修改任务名称
     * @param jobName
     * @return
     */
    public RequestUpdateHandler setJobName(String jobName) {
        requestUpdateJobDTO.setJobName(jobName);
        return this;
    }

    /**
     * 修改时会直接覆盖之前的任务参数
     * 修改参数
     * @param argsStr
     * @return
     */
    private RequestUpdateHandler setArgsStr(Map<String, Object> argsStr) {
        Map<String, Object> args = new HashMap<>();
        if (StrUtil.isNotBlank(requestUpdateJobDTO.getArgsStr())){
            args = JsonUtil.parseHashMap(requestUpdateJobDTO.getArgsStr());
        }
        args.putAll(argsStr);
        requestUpdateJobDTO.setArgsStr(JsonUtil.toJsonString(args));
        requestUpdateJobDTO.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        return this;
    }

    /**
     * 修改时会直接覆盖之前的任务参数
     * 添加参数，可支持多次添加
     * 静态分片不可使用该方法
     * @param argsKey 参数名
     * @param argsValue 参数值
     * @return
     */
    public RequestUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        if (requestUpdateJobDTO.getTaskType() != null
                && requestUpdateJobDTO.getTaskType().equals(JobTaskTypeEnum.SHARDING.getType())){
            SnailJobLog.LOCAL.warn("静态分片任务，不可使用该方法添加相关任务参数，请使用addShardingArgs");
            return this;
        }
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(requestUpdateJobDTO.getArgsStr())){
            map = JsonUtil.parseHashMap(requestUpdateJobDTO.getArgsStr());
        }
        map.put(argsKey, argsValue);
        requestUpdateJobDTO.setArgsStr(JsonUtil.toJsonString(map));
        requestUpdateJobDTO.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        return this;
    }

    /**
     * 添加静态分片相关参数
     * @param shardingValue
     * @return
     */
    public RequestUpdateHandler addShardingArgs(String[] shardingValue){
        if (requestUpdateJobDTO.getTaskType() != null
                && !requestUpdateJobDTO.getTaskType().equals(JobTaskTypeEnum.SHARDING.getType())){
            SnailJobLog.LOCAL.warn("非静态分片任务，不可使用该方法添加相关任务参数，请使用addArgsStr");
            return this;
        }
        requestUpdateJobDTO.setArgsStr(JsonUtil.toJsonString(shardingValue));
        requestUpdateJobDTO.setArgsType(JobArgsTypeEnum.TEXT.getArgsType());
        return this;
    }

    /**
     * 修改路由
     * @param algorithmEnum
     * @return
     */
    public RequestUpdateHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        requestUpdateJobDTO.setRouteKey(algorithmEnum.getType());
        return this;
    }

    /**
     * 修改相关执行器
     * @param executorInfo
     * @return
     */
    public RequestUpdateHandler setExecutorInfo(String executorInfo) {
        requestUpdateJobDTO.setExecutorInfo(executorInfo);
        return this;
    }

    /**
     * 修改调度类型
     * @param triggerType
     * @return
     */
    public RequestUpdateHandler setTriggerType(TriggerTypeEnum triggerType) {
        requestUpdateJobDTO.setTriggerType(triggerType.getType());
        return this;
    }

    /**
     * 修改调度时间
     * 单位：秒
     * 工作流无需配置
     * @param triggerInterval
     * @return
     */
    public RequestUpdateHandler setTriggerInterval(String triggerInterval) {
        requestUpdateJobDTO.setTriggerInterval(triggerInterval);
        return this;
    }

    /**
     * 修改阻塞策略
     * @param blockStrategy
     * @return
     */
    public RequestUpdateHandler setBlockStrategy(BlockStrategyEnum blockStrategy) {
        requestUpdateJobDTO.setBlockStrategy(blockStrategy.getBlockStrategy());
        return this;
    }

    /**
     * 修改执行器超时时间
     * @param executorTimeout
     * @return
     */
    public RequestUpdateHandler setExecutorTimeout(Integer executorTimeout) {
        requestUpdateJobDTO.setExecutorTimeout(executorTimeout);
        return this;
    }

    /**
     * 修改任务最大重试次数
     * @param maxRetryTimes
     * @return
     */
    public RequestUpdateHandler setMaxRetryTimes(Integer maxRetryTimes) {
        requestUpdateJobDTO.setMaxRetryTimes(maxRetryTimes);
        return this;
    }

    /**
     * 修改重试间隔
     * @param retryInterval
     * @return
     */
    public RequestUpdateHandler setRetryInterval(Integer retryInterval) {
        requestUpdateJobDTO.setRetryInterval(retryInterval);
        return this;
    }

    /**
     * 修改并发数量
     * @param parallelNum
     * @return
     */
    public RequestUpdateHandler setParallelNum(Integer parallelNum) {
        requestUpdateJobDTO.setParallelNum(parallelNum);
        return this;
    }

    /**
     * 修改定时任务描述
     * @param description
     * @return
     */
    public RequestUpdateHandler setDescription(String description) {
        requestUpdateJobDTO.setDescription(description);
        return this;
    }


}
