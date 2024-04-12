package com.aizuda.easy.retry.common.core.alarm.strategy;

import cn.hutool.extra.mail.MailUtil;
import com.aizuda.easy.retry.common.core.alarm.EmailAttribute;
import com.aizuda.easy.retry.common.core.enums.AlarmTypeEnum;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2021-11-25 09:20
 */
@Component
public class EmailAlarm extends AbstractAlarm<AlarmContext> {

    @Override
    public Integer getAlarmType() {
        return AlarmTypeEnum.EMAIL.getValue();
    }

    @Override
    public boolean asyncSendMessage(AlarmContext alarmContext) {
        threadPoolExecutor.execute(() -> syncSendMessage(alarmContext));
        return true;
    }

    @Override
    public boolean syncSendMessage(AlarmContext alarmContext) {

        String notifyAttribute = alarmContext.getNotifyAttribute();
        EmailAttribute emailAttribute = JsonUtil.parseObject(notifyAttribute, EmailAttribute.class);
        emailAttribute.setAuth(true);
        String text = alarmContext.getText();
        text = text.replaceAll("\n", "<br/>");
        MailUtil.send(emailAttribute, emailAttribute.getTos(), alarmContext.getTitle(), text, true);

        return true;
    }

    @Override
    public boolean asyncSendMessage(List<AlarmContext> alarmContexts) {
        for (AlarmContext alarmContext : alarmContexts) {
            asyncSendMessage(alarmContext);
        }

        return Boolean.TRUE;
    }

}

