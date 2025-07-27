package com.aizuda.snailjob.server.web.model.response;

import com.aizuda.snailjob.model.base.RetryResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryResponseWebVO extends RetryResponse {
    private List<RetryResponseWebVO> children;

}
