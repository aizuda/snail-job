package com.aizuda.easy.retry.common.core.exception;

import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2020/05/13
 */
@Data
public abstract class AbstractError {

    public static final AbstractError SUCCESS = new AbstractError("操作成功", "success") {
    };

    public static final AbstractError ERROR = new AbstractError("操作失败", "error") {
    };

    public static final AbstractError PARAM_INCORRECT = new AbstractError("参数异常", "param incorrect") {
    };

    private final String zhMsg;

    private final String enMsg;

    protected AbstractError(String zhMsg, String enMsg) {
        this.zhMsg = zhMsg;
        this.enMsg = enMsg;
    }

    public static String getMsg(AbstractError abstractError) {
        return abstractError.zhMsg;
    }

    @Override
    public String toString() {
        return this.zhMsg;
    }
}
