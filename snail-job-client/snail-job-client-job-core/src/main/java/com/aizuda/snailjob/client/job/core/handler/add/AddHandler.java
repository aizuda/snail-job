package com.aizuda.snailjob.client.job.core.handler.add;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.RequestAddJobDTO;
import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum;
import com.aizuda.snailjob.client.job.core.handler.AbstractRequestHandler;
import com.aizuda.snailjob.client.job.core.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.*;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import static com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum.WORK_FLOW;

public abstract class AddHandler<R> extends AbstractRequestHandler<Long> {
    private final RequestAddJobDTO reqDTO;
    @Setter
    private R r;
    public AddHandler(JobTaskTypeEnum taskType, Integer shardNum) {
        this.reqDTO = new RequestAddJobDTO();
        // 默认创建就开启
        reqDTO.setJobStatus(StatusEnum.YES.getStatus());
        // 设置任务类型
        reqDTO.setTaskType(taskType.getType());
        // 默认java
        reqDTO.setExecutorType(ExecutorTypeEnum.JAVA.getType());
        // 设置分片
        if (shardNum != null) {
            Map<String, Object> map = new HashMap<>(1);
            map.put(SHARD_NUM, shardNum);
            reqDTO.setArgsStr(JsonUtil.toJsonString(map));
        }
    }

    @Override
    protected Long doExecute() {
        String data = JsonUtil.toJsonString(client.addJob(reqDTO).getData());
        return Long.valueOf(data);
    }

    @Override
    protected void beforeExecute() {
        // 此次是兜底覆盖,工作流是没有调度时间
        if (reqDTO.getTriggerType() == WORK_FLOW.getType()) {
            setTriggerInterval("*");
        }
    }

    @Override
    protected void afterExecute(Long aLong) {
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(reqDTO);
    }

    /**
     * 设置任务名
     *
     * @param jobName 任务名
     * @return
     */
    public R setJobName(String jobName) {
        reqDTO.setJobName(jobName);
        return r;
    }

    /**
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
     *
     * @param shardingValue 分片参数
     * @return r
     */
    protected R addShardingArgs(String ...shardingValue) {
        reqDTO.setArgsStr(JsonUtil.toJsonString(shardingValue));
        reqDTO.setArgsType(JobArgsTypeEnum.TEXT.getArgsType());
        return r;
    }

    /**
     * 设置路由
     *
     * @param algorithmEnum 路由算法
     * @return r
     */
    protected R setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        reqDTO.setRouteKey(algorithmEnum.getType());
        return r;
    }

    /**
     * 设置执行器信息
     *
     * @param executorInfo 执行器信息
     * @return r
     */
    public R setExecutorInfo(String executorInfo) {
        reqDTO.setExecutorInfo(executorInfo);
        return r;
    }

    /**
     * 设置调度类型
     *
     * @param triggerType 触发类型
     * @return r
     */
    public R setTriggerType(TriggerTypeEnum triggerType) {
        reqDTO.setTriggerType(triggerType.getType());
        return r;
    }

    /**
     * 设置触发间隔；
     * 单位：秒
     * 工作流无需配置
     *
     * @param triggerInterval 触发间隔
     * @return r
     */
    public R setTriggerInterval(String triggerInterval) {
        // 若是工作流则没有调度时间
        Assert.isTrue(reqDTO.getTriggerType() == WORK_FLOW.getType(),
                () -> new SnailJobClientException("工作流无需配置"));
        reqDTO.setTriggerInterval(triggerInterval);
        return r;
    }

    /**
     * 设置阻塞策略
     *
     * @param blockStrategy 阻塞策略
     * @return r
     */
    public R setBlockStrategy(BlockStrategyEnum blockStrategy) {
        reqDTO.setBlockStrategy(blockStrategy.getBlockStrategy());
        return r;
    }

    /**
     * 设置执行器超时时间
     *
     * @param executorTimeout 超时时间(单位:秒)
     * @return r
     */
    public R setExecutorTimeout(Integer executorTimeout) {
        reqDTO.setExecutorTimeout(executorTimeout);
        return r;
    }

    /**
     * 设置任务最大重试次数
     *
     * @param maxRetryTimes 最大超时时间
     * @return r
     */
    public R setMaxRetryTimes(Integer maxRetryTimes) {
        reqDTO.setMaxRetryTimes(maxRetryTimes);
        return r;
    }

    /**
     * 设置重试间隔
     *
     * @param retryInterval 重试间隔
     * @return r
     */
    public R setRetryInterval(Integer retryInterval) {
        reqDTO.setRetryInterval(retryInterval);
        return r;
    }

    /**
     * 设置并发数量
     *
     * @param parallelNum 并发数量
     * @return r
     */
    protected R setParallelNum(Integer parallelNum) {
        reqDTO.setParallelNum(parallelNum);
        return r;
    }

    /**
     * 设置定时任务描述
     *
     * @param description 任务描述
     * @return r
     */
    public R setDescription(String description) {
        reqDTO.setDescription(description);
        return r;
    }

}
