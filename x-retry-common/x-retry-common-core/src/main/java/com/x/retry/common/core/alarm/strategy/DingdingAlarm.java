package com.x.retry.common.core.alarm.strategy;

import com.x.retry.common.core.alarm.AlarmContext;
import com.x.retry.common.core.enums.AlarmTypeEnum;
import com.x.retry.common.core.util.DingDingUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-25 09:20
 */
@Component
public class DingdingAlarm extends AbstractAlarm<AlarmContext> {

    @Override
    public Integer getAlarmType() {
        return AlarmTypeEnum.DING_DING.getValue();
    }

    @Override
    public boolean asyncSendMessage(AlarmContext context) {

        threadPoolExecutor.execute(() ->
                DingDingUtils.sendMessage(DingDingUtils.buildSendRequest(context.getTitle(), context.getText()), context.getNotifyAddress()));

        return true;
    }

    @Override
    public boolean syncSendMessage(AlarmContext context) {
        return DingDingUtils.sendMessage(DingDingUtils.buildSendRequest(context.getTitle(), context.getText()), context.getNotifyAddress());
    }

    @Override
    public boolean asyncSendMessage(List<AlarmContext> alarmContexts) {

        for (AlarmContext alarmContext : alarmContexts) {
            asyncSendMessage(alarmContext);
        }

        return Boolean.TRUE;
    }
}

