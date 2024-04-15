package com.aizuda.snail.job.common.core.alarm;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2021-11-25 09:19
 */
public interface Alarm<T> {

    Integer getAlarmType();

    boolean asyncSendMessage(T t);

    boolean syncSendMessage(T t);

    boolean asyncSendMessage(List<T> ts);
}
