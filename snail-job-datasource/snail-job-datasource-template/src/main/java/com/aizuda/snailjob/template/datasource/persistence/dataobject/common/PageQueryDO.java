package com.aizuda.snailjob.template.datasource.persistence.dataobject.common;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-30
 */
@Data
public class PageQueryDO {

    /**
     * 当前页码
     */
    private int page = 1;

    /**
     * 每页条数
     */
    private int size = 10;
}
