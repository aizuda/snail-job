package com.aizuda.snailjob.client.job.core.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.RequestAddOrUpdateJobDTO;
import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum;
import com.aizuda.snailjob.common.core.enums.*;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;
import java.util.Map;

import static com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum.*;

/**
 * @author opensnail
 * @date 2024-10-19 22:34:38
 * @since sj_1.2.0
 */
public abstract class AbstractParamsHandler<H, R> extends AbstractRequestHandler<R> {
    protected static final String SHARD_NUM = "shardNum";
    @Getter
    private final RequestAddOrUpdateJobDTO reqDTO;
    @Setter
    private H r;

    public AbstractParamsHandler(JobTaskTypeEnum taskType) {
        this.reqDTO = new RequestAddOrUpdateJobDTO();
        // 默认创建就开启
        reqDTO.setJobStatus(StatusEnum.YES.getStatus());
        // 设置任务类型
        reqDTO.setTaskType(taskType.getType());
        // 默认java
        reqDTO.setExecutorType(ExecutorTypeEnum.JAVA.getType());
    }

    protected H setId(Long id) {
        reqDTO.setId(id);
        return r;
    }

    /**
     * 修改时会直接覆盖之前的任务参数
     * 修改参数
     *
     * @param argsStr
     * @return
     */
    private H setArgsStr(Map<String, Object> argsStr) {
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
     * 修改Reduce的分片数
     * 只允许MAP_REDUCE设置
     *
     * @param shardNum
     * @return
     */
    protected H setShardNum(Integer shardNum) {
        // 设置分片
        if (shardNum != null) {
            Map<String, Object> map = new HashMap<>(1);
            map.put(SHARD_NUM, shardNum);
            setArgsStr(map);
        }
        return r;
    }

    /**
     * 设置任务名
     *
     * @param jobName 任务名
     * @return
     */
    public H setJobName(String jobName) {
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
    protected H addArgsStr(String argsKey, Object argsValue) {
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
    protected H addShardingArgs(String... shardingValue) {
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
    protected H setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        reqDTO.setRouteKey(algorithmEnum.getType());
        return r;
    }

    /**
     * 设置执行器信息
     *
     * @param executorInfo 执行器信息
     * @return r
     */
    public H setExecutorInfo(String executorInfo) {
        reqDTO.setExecutorInfo(executorInfo);
        return r;
    }

    /**
     * 设置调度类型
     *
     * @param triggerType 触发类型
     * @return r
     */
    public H setTriggerType(TriggerTypeEnum triggerType) {
        reqDTO.setTriggerType(triggerType.getType());
        return r;
    }

    /**
     * 设置触发间隔；
     * 单位：秒
     * 注意: 此方法必须满足【triggerType==SCHEDULED_TIME】
     *
     * @param triggerInterval 触发间隔
     * @return r
     */
    public H setTriggerInterval(Integer triggerInterval) {
        Assert.isTrue(reqDTO.getTriggerType() == SCHEDULED_TIME.getType(),
                () -> new SnailJobClientException("此方法只限制固定时间使用"));
        setTriggerInterval(String.valueOf(triggerInterval));
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
    public H setTriggerInterval(String triggerInterval) {
        // 若是工作流则没有调度时间
        Assert.isFalse(reqDTO.getTriggerType() == WORK_FLOW.getType(),
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
    public H setBlockStrategy(BlockStrategyEnum blockStrategy) {
        reqDTO.setBlockStrategy(blockStrategy.getBlockStrategy());
        return r;
    }

    /**
     * 设置执行器超时时间
     *
     * @param executorTimeout 超时时间(单位:秒)
     * @return r
     */
    public H setExecutorTimeout(Integer executorTimeout) {
        reqDTO.setExecutorTimeout(executorTimeout);
        return r;
    }

    /**
     * 设置任务最大重试次数
     *
     * @param maxRetryTimes 最大超时时间
     * @return r
     */
    public H setMaxRetryTimes(Integer maxRetryTimes) {
        reqDTO.setMaxRetryTimes(maxRetryTimes);
        return r;
    }

    /**
     * 设置重试间隔
     *
     * @param retryInterval 重试间隔
     * @return r
     */
    public H setRetryInterval(Integer retryInterval) {
        reqDTO.setRetryInterval(retryInterval);
        return r;
    }

    /**
     * 设置并发数量
     *
     * @param parallelNum 并发数量
     * @return r
     */
    protected H setParallelNum(Integer parallelNum) {
        reqDTO.setParallelNum(parallelNum);
        return r;
    }

    /**
     * 设置定时任务描述
     *
     * @param description 任务描述
     * @return r
     */
    public H setDescription(String description) {
        reqDTO.setDescription(description);
        return r;
    }

}
