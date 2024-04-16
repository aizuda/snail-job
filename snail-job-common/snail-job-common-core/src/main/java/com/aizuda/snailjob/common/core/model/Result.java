package com.aizuda.snailjob.common.core.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: opensnail
 * @date : 2022-02-16 14:07
 */
@Data
@Accessors(chain = true)
public class Result<T> {

    protected int status = 1;

    protected String message;

    protected T data;

    public Result() {
    }

    public Result(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Result(T data) {
        this.data = data;
    }

    public Result(String message, T data) {
        this.data = data;
        this.message = message;
    }

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
