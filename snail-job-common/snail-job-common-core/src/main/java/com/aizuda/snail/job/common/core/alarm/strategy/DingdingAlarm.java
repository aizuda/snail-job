package com.aizuda.snail.job.common.core.alarm.strategy;

import com.aizuda.snail.job.common.core.enums.AlarmTypeEnum;
import com.aizuda.snail.job.common.core.alarm.AlarmContext;
import com.aizuda.snail.job.common.core.alarm.DingDingAttribute;
import com.aizuda.snail.job.common.core.util.DingDingUtils;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: opensnail
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
        threadPoolExecutor.execute(() -> syncSendMessage(context));
        return true;
    }

    @Override
    public boolean syncSendMessage(AlarmContext context) {
        DingDingAttribute dingDingAttribute = JsonUtil.parseObject(context.getNotifyAttribute(), DingDingAttribute.class);
        return DingDingUtils.sendMessage(DingDingUtils.buildSendRequest(context.getTitle(), context.getText(),dingDingAttribute.getAts()), dingDingAttribute.getWebhookUrl());
    }

    @Override
    public boolean asyncSendMessage(List<AlarmContext> alarmContexts) {

        for (AlarmContext alarmContext : alarmContexts) {
            asyncSendMessage(alarmContext);
        }

        return Boolean.TRUE;
    }
}

