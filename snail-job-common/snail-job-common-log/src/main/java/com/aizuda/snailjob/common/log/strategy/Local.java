package com.aizuda.snailjob.common.log.strategy;

/**
 * @author zhengweilin
 * @version 2.6.0
 * @date 2024/01/09
 */
public final class Local extends AbstractLog {
    public Local() {
        setRemote(Boolean.FALSE);
    }
}
