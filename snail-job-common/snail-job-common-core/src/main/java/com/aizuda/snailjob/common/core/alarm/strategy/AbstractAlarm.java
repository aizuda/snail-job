package com.aizuda.snailjob.common.core.alarm.strategy;

import com.aizuda.snailjob.common.core.alarm.Alarm;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: opensnail
 * @date : 2021-11-25 09:19
 */
public abstract class AbstractAlarm<T> implements Alarm<T>, InitializingBean {

    protected static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Override
    public void afterPropertiesSet() throws Exception {
        SnailJobAlarmFactory.register(this);
    }

    @Override
    public boolean asyncSendMessage(T t) {
        threadPoolExecutor.execute(() -> syncSendMessage(t));
        return true;
    }

}
