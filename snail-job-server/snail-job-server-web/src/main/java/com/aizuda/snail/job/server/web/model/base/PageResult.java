package com.aizuda.snail.job.server.web.model.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.snail.job.common.core.model.Result;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: opensnail
 * @date : 2022-02-16 14:07
 */
@Data
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
