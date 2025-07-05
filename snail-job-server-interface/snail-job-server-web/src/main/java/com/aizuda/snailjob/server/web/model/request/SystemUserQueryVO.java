package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.common.vo.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author opensnail
 * @date 2022-03-06
 * @since 2.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemUserQueryVO extends BaseQueryVO {

    private String username;

}
