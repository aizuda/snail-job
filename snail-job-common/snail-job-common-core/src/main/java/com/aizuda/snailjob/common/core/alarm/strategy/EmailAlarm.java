package com.aizuda.snailjob.common.core.alarm.strategy;

import com.aizuda.snailjob.common.core.alarm.attribute.EmailAttribute;
import com.aizuda.snailjob.common.core.alarm.email.MailAccount;
import com.aizuda.snailjob.common.core.alarm.email.MailProperties;
import com.aizuda.snailjob.common.core.util.MailUtils;
import com.aizuda.snailjob.common.core.enums.AlarmTypeEnum;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author: opensnail
 * @date : 2021-11-25 09:20
 */
@Component
@RequiredArgsConstructor
public class EmailAlarm extends AbstractAlarm<AlarmContext> {
    private final MailProperties mailProperties;
    private MailAccount mailAccount;

    @Override
    public Integer getAlarmType() {
        return AlarmTypeEnum.EMAIL.getValue();
    }

    @Override
    public boolean syncSendMessage(AlarmContext alarmContext) {
        if (Objects.isNull(mailAccount)) {
            SnailJobLog.LOCAL.warn("请检查邮件配置是否开启");
            return false;
        }

        try {
            String notifyAttribute = alarmContext.getNotifyAttribute();
            EmailAttribute emailAttribute = JsonUtil.parseObject(notifyAttribute, EmailAttribute.class);
            String text = alarmContext.getText();
            text = text.replaceAll("\n", "<br/>");
            MailUtils.send(mailAccount, emailAttribute.getTos(), alarmContext.getTitle(), text, true);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("发送email消息失败:", e);
            return false;
        }

        return true;
    }

    @Override
    public boolean asyncSendMessage(List<AlarmContext> alarmContexts) {
        for (AlarmContext alarmContext : alarmContexts) {
            asyncSendMessage(alarmContext);
        }

        return Boolean.TRUE;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Boolean enabled = mailProperties.getEnabled();
        if (Objects.isNull(enabled) || Boolean.FALSE.equals(enabled)) {
            return;
        }

        mailAccount = initMailAccount(mailProperties);
        MailUtils.setMailAccount(mailAccount);
    }

    private MailAccount initMailAccount(MailProperties mailProperties) {
        MailAccount account = new MailAccount();
        account.setHost(mailProperties.getHost());
        account.setPort(mailProperties.getPort());
        account.setAuth(Optional.ofNullable(mailProperties.getAuth()).orElse(Boolean.FALSE));
        account.setFrom(mailProperties.getFrom());
        account.setUser(mailProperties.getUser());
        account.setPass(mailProperties.getPass());
        account.setSocketFactoryPort(Optional.ofNullable(mailProperties.getPort()).orElse(465));
        account.setStarttlsEnable(Optional.ofNullable(mailProperties.getStarttlsEnable()).orElse(Boolean.FALSE));
        account.setSslEnable(Optional.ofNullable(mailProperties.getSslEnable()).orElse(Boolean.FALSE));
        account.setTimeout(Optional.ofNullable(mailProperties.getTimeout()).orElse(0L));
        account.setConnectionTimeout(Optional.ofNullable(mailProperties.getConnectionTimeout()).orElse(0L));
        return account;
    }
}

