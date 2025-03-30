package com.aizuda.snailjob.client.job.core.handler;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.job.core.dto.JobTriggerDTO;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractTriggerHandler<H, R> extends AbstractJobRequestHandler<R> {
    @Getter
    private final JobTriggerDTO reqDTO;
    @Setter
    private H r;

    public AbstractTriggerHandler(Long jobId) {
        this.reqDTO = new JobTriggerDTO();
       // 设置调度id
        reqDTO.setJobId(jobId);
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
        if (StrUtil.isNotBlank(reqDTO.getTmpArgsStr())) {
            map = JsonUtil.parseHashMap(reqDTO.getTmpArgsStr());
        }
        map.put(argsKey, argsValue);
        reqDTO.setTmpArgsStr(JsonUtil.toJsonString(map));
        return r;
    }

    /**
     * 添加静态分片相关参数
     *
     * @param shardingValue 分片参数
     * @return r
     */
    protected H addShardingArgs(String... shardingValue) {
        reqDTO.setTmpArgsStr(JsonUtil.toJsonString(shardingValue));
        return r;
    }

    protected abstract void afterExecute(Boolean aBoolean);
}
