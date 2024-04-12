package com.aizuda.easy.retry.server.web.model.base;

import lombok.Data;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
@Data
public class BaseQueryVO {

    private int page = 1;

    private int size = 10;

}
