package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2022-03-06
 * @since 2.0
 */
@Data
public class SystemUserQueryVO extends BaseQueryVO {

    private String username;

}
