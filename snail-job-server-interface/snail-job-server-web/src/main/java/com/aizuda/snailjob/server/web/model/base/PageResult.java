package com.aizuda.snailjob.server.web.model.base;

import com.aizuda.snailjob.common.core.model.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: opensnail
 * @date : 2022-02-16 14:07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class PageResult<T> extends Result<T> {

    private long page;
    private long size;
    private long total;

    public PageResult(int status, String message, T data) {
        super(status, message, data);
    }

    public PageResult() {
        super();
    }

    public PageResult(PageDTO pageDTO, T data) {
        page = pageDTO.getCurrent();
        size = pageDTO.getSize();
        total = pageDTO.getTotal();
        super.setData(data);
    }

}
