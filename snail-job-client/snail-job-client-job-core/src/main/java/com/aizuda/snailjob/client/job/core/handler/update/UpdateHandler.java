package com.aizuda.snailjob.client.job.core.handler.update;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.job.core.dto.RequestUpdateJobDTO;
import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum;
import com.aizuda.snailjob.client.job.core.handler.AbstractRequestHandler;
import com.aizuda.snailjob.client.job.core.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.BlockStrategyEnum;
import com.aizuda.snailjob.common.core.enums.JobArgsTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


public abstract class UpdateHandler<R> extends AbstractRequestHandler<Boolean> {
    private final RequestUpdateJobDTO reqDTO;
    @Setter
    private R r;

    public UpdateHandler(Long jobId) {
        this.reqDTO = new RequestUpdateJobDTO();
        // 更新必须要id
        reqDTO.setId(jobId);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {
        if (reqDTO.getTriggerType() == TriggerTypeEnum.WORK_FLOW.getType()) {
            // 工作流没有调度时间
            setTriggerInterval("*");
        }
    }

    @Override
    protected Boolean doExecute() {
        return (Boolean) client.updateJob(reqDTO).getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(reqDTO);
    }

    /**
     * 修改Reduce的分片数
     * 只允许MAP_REDUCE设置
     *
     * @param shardNum
     * @return
     */
    protected R setShardNum(Integer shardNum) {
        // 设置分片
        if (shardNum != null) {
            Map<String, Object> map = new HashMap<>(1);
            map.put(SHARD_NUM, shardNum);
            setArgsStr(map);
        }
        return r;
    }

    /**
     * 修改任务名称
     *
     * @param jobName
     * @return
     */
    public R setJobName(String jobName) {
        reqDTO.setJobName(jobName);
        return r;
    }

    /**
     * 修改时会直接覆盖之前的任务参数
     * 修改参数
     *
     * @param argsStr
     * @return
     */
    private R setArgsStr(Map<String, Object> argsStr) {
        Map<String, Object> args = new HashMap<>();
        if (StrUtil.isNotBlank(reqDTO.getArgsStr())) {
            args = JsonUtil.parseHashMap(reqDTO.getArgsStr());
        }
        args.putAll(argsStr);
        reqDTO.setArgsStr(JsonUtil.toJsonString(args));
        reqDTO.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        return r;
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
    protected R addArgsStr(String argsKey, Object argsValue) {
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(reqDTO.getArgsStr())) {
            map = JsonUtil.parseHashMap(reqDTO.getArgsStr());
        }
        map.put(argsKey, argsValue);
        reqDTO.setArgsStr(JsonUtil.toJsonString(map));
        reqDTO.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        return r;
    }

    /**
     * 添加静态分片相关参数
     * 只有静态分片任务可用
     *
     * @param shardingValue
     * @return
     */
    protected R addShardingArgs(String[] shardingValue) {
        reqDTO.setArgsStr(JsonUtil.toJsonString(shardingValue));
        reqDTO.setArgsType(JobArgsTypeEnum.TEXT.getArgsType());
        return r;
    }

    /**
     * 修改路由
     *
     * @param algorithmEnum
     * @return
     */
    protected R setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        reqDTO.setRouteKey(algorithmEnum.getType());
        return r;
    }

    /**
     * 修改相关执行器
     *
     * @param executorInfo
     * @return
     */
    public R setExecutorInfo(String executorInfo) {
        reqDTO.setExecutorInfo(executorInfo);
        return r;
    }

    /**
     * 修改调度类型
     *
     * @param triggerType
     * @return
     */
    public R setTriggerType(TriggerTypeEnum triggerType) {
        reqDTO.setTriggerType(triggerType.getType());
        return r;
    }

    /**
     * 修改调度时间
     * 单位：秒
     * 工作流无需配置
     *
     * @param triggerInterval
     * @return
     */
    public R setTriggerInterval(String triggerInterval) {
        reqDTO.setTriggerInterval(triggerInterval);
        return r;
    }

    /**
     * 修改阻塞策略
     *
     * @param blockStrategy
     * @return
     */
    public R setBlockStrategy(BlockStrategyEnum blockStrategy) {
        reqDTO.setBlockStrategy(blockStrategy.getBlockStrategy());
        return r;
    }

    /**
     * 修改执行器超时时间
     *
     * @param executorTimeout
     * @return
     */
    public R setExecutorTimeout(Integer executorTimeout) {
        reqDTO.setExecutorTimeout(executorTimeout);
        return r;
    }

    /**
     * 修改任务最大重试次数
     *
     * @param maxRetryTimes
     * @return
     */
    public R setMaxRetryTimes(Integer maxRetryTimes) {
        reqDTO.setMaxRetryTimes(maxRetryTimes);
        return r;
    }

    /**
     * 修改重试间隔
     *
     * @param retryInterval
     * @return
     */
    public R setRetryInterval(Integer retryInterval) {
        reqDTO.setRetryInterval(retryInterval);
        return r;
    }

    /**
     * 修改并发数量
     *
     * @param parallelNum
     * @return
     */
    protected R setParallelNum(Integer parallelNum) {
        reqDTO.setParallelNum(parallelNum);
        return r;
    }

    /**
     * 修改定时任务描述
     *
     * @param description
     * @return
     */
    public R setDescription(String description) {
        reqDTO.setDescription(description);
        return r;
    }


}
