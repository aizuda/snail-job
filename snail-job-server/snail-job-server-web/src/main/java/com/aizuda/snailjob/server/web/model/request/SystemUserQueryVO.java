package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import lombok.Data;

/**
 * @author opensnail
 * @date 2022-03-06
 * @since 2.0
 */
@Data
public class SystemUserQueryVO extends BaseQueryVO {

    private String username;

}
