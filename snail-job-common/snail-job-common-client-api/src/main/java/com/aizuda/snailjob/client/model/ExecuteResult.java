package com.aizuda.snailjob.client.model;

import com.aizuda.snailjob.common.core.enums.StatusEnum;

/**
 * @author: opensnail
 * @date : 2023-09-27 09:43
 * @since 2.4.0
 */
public class ExecuteResult {
    private int status;
    private Object result;
    private String message;

    public ExecuteResult() {
    }

    public ExecuteResult(int status, Object result, String message) {
        this.status = status;
        this.result = result;
        this.message = message;
    }

    public static ExecuteResult success(Object result) {
        return new ExecuteResult(StatusEnum.YES.getStatus(), result, "任务执行成功");
    }

    public static ExecuteResult success() {
        return success(null);
    }

    public static ExecuteResult failure() {
        return failure(null);
    }

    public static ExecuteResult failure(Object result) {
        return new ExecuteResult(StatusEnum.NO.getStatus(), result, "任务执行失败");
    }

    public static ExecuteResult failure(Object result, String message) {
        return new ExecuteResult(StatusEnum.NO.getStatus(), result, message);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
