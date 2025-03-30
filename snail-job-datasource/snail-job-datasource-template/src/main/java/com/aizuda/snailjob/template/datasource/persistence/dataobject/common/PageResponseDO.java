package com.aizuda.snailjob.template.datasource.persistence.dataobject.common;

import lombok.Data;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-30
 */
@Data
public class PageResponseDO<T> {

    private List<T> rows;

    private long total;
    private long page;
    private long size;
}
