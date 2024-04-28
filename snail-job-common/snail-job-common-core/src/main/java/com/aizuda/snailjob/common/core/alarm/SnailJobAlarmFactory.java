package com.aizuda.snailjob.common.core.alarm;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: opensnail
 * @date : 2021-11-25 09:20
 */
@Getter
@Component
public class SnailJobAlarmFactory {

    private final Map<Integer, Alarm> alarmMap = new ConcurrentHashMap<>();

    @Autowired
    public SnailJobAlarmFactory(Map<String, Alarm> map) {
        for (Map.Entry<String, Alarm> entry : map.entrySet()) {
            alarmMap.put(entry.getValue().getAlarmType(), entry.getValue());
        }
    }

    public Alarm getAlarmType(Integer alarmType) {
        return alarmMap.get(alarmType);
    }
}
