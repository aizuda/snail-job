package com.aizuda.easy.retry.common.core.alarm;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-25 09:19
 */
public interface Alarm<T> {

    Integer getAlarmType();

    boolean asyncSendMessage(T t);

    boolean syncSendMessage(T t);

    boolean asyncSendMessage(List<T> ts);
}
