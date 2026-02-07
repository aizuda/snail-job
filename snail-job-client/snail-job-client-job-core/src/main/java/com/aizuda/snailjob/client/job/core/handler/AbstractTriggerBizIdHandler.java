package com.aizuda.snailjob.client.job.core.handler;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.model.request.JobTriggerBizIdRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


/**
 * Abstract handler for BizId-based job trigger operations.
 * Similar to AbstractTriggerHandler but uses JobTriggerBizIdRequest.
 *
 * @author opensnail
 * @since sj_1.2.0
 */
public abstract class AbstractTriggerBizIdHandler<H, R> extends AbstractJobRequestHandler<R> {
    @Getter
    private final JobTriggerBizIdRequest reqDTO;
    @Setter
    private H r;

    public AbstractTriggerBizIdHandler(String bizId) {
        this.reqDTO = new JobTriggerBizIdRequest();
        // 设置业务ID
        reqDTO.setBizId(bizId);
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