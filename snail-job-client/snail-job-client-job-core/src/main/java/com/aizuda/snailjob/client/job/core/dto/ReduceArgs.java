package com.aizuda.snailjob.client.job.core.dto;

import com.aizuda.snailjob.common.core.util.JsonUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Task执行结果
 *
 * @author: opensnail
 * @date : 2024-06-12 13:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReduceArgs extends JobArgs {

    private List<?> mapResult;

    public List<?> getMapResult() {
        return JsonUtil.parseList(getArgsStr(), List.class);
    }
}
