package com.x.retry.common.core.alarm.strategy;

import com.x.retry.common.core.enums.AlarmTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-25 09:20
 */
@Component
public class EmailAlarm extends AbstractAlarm {

    @Override
    public Integer getAlarmType() {
        return AlarmTypeEnum.EMAIL.getValue();
    }

    @Override
    public boolean asyncSendMessage(Object o) {
        return false;
    }

    @Override
    public boolean syncSendMessage(Object o) {
        return false;
    }

    @Override
    public boolean asyncSendMessage(List list) {
        return false;
    }
}

