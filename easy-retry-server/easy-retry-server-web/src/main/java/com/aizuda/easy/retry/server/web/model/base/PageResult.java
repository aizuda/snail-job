package com.aizuda.easy.retry.server.web.model.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.common.core.model.Result;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: www.byteblogs.com
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
