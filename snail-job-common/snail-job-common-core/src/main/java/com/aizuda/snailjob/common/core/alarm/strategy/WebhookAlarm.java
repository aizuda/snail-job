package com.aizuda.snailjob.common.core.alarm.strategy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.attribute.WebhookAttribute;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.AlarmTypeEnum;
import com.aizuda.snailjob.common.core.enums.ContentTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2024-05-07 10:15
 */
@Component
@RequiredArgsConstructor
public class WebhookAlarm extends AbstractAlarm<AlarmContext> {

    @Override
    public Integer getAlarmType() {
        return AlarmTypeEnum.WEBHOOK.getValue();
    }

    @Override
    public boolean syncSendMessage(AlarmContext alarmContext) {

        WebhookAttribute webhookAttribute = JsonUtil.parseObject(alarmContext.getNotifyAttribute(), WebhookAttribute.class);
        for (int i = 0; i < 2; i++) {
            try {
                WebhookMessage webhookMessage = WebhookMessage.builder().text(alarmContext.getTitle()).build();

                HttpRequest post = HttpUtil.createPost(webhookAttribute.getWebhookUrl());
                HttpRequest request = post.body(JsonUtil.toJsonString(webhookMessage), ContentTypeEnum.valueOf(webhookAttribute.getContentType()).getMediaType().toString())
                        .header(SystemConstants.SECRET, webhookAttribute.getSecret());
                HttpResponse execute = request.execute();
                if (execute.isOk()) {
                    return true;
                }
                SnailJobLog.LOCAL.info("发送Webhook告警结果. webHook:[{}]，结果: [{}]", webhookAttribute.getWebhookUrl(), execute.body());
                break;
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("发送Webhook告警异常. webHook:[{}]", webhookAttribute, e);
            }
        }

        return true;
    }

    @Data
    @Builder
    private static class WebhookMessage {

        private String text;
    }

    @Override
    public boolean asyncSendMessage(List<AlarmContext> alarmContexts) {
        for (AlarmContext alarmContext : alarmContexts) {
            asyncSendMessage(alarmContext);
        }

        return true;
    }
}

