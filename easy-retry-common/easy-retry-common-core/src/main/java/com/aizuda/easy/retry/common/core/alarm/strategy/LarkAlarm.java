package com.aizuda.easy.retry.common.core.alarm.strategy;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.LarkAttribute;
import com.aizuda.easy.retry.common.core.enums.AlarmTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 飞书通知
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-25 09:20
 * @since 1.4.0
 */
@Component
@Slf4j
public class LarkAlarm extends AbstractAlarm<AlarmContext> {

    @Override
    public Integer getAlarmType() {
        return AlarmTypeEnum.LARK.getValue();
    }

    @Override
    public boolean asyncSendMessage(AlarmContext context) {
        threadPoolExecutor.execute(() -> {
            syncSendMessage(context);
        });

        return true;
    }

    @Override
    public boolean syncSendMessage(AlarmContext context) {

        LarkAttribute larkAttribute = JsonUtil.parseObject(context.getNotifyAttribute(), LarkAttribute.class);

        Map<String, Object> map = new HashMap<>();
        map.put("header", buildHeader(context.getTitle()));
        map.put("elements", buildElements(context.getText()));

        LarkMessage builder = LarkMessage.builder()
            .msgType("interactive")
            .card(map).build();

        try {
            HttpRequest post = HttpUtil.createPost(larkAttribute.getLarkUrl());
            HttpRequest request = post.body(JsonUtil.toJsonString(builder), ContentType.JSON.toString());
            HttpResponse execute = request.execute();
            LogUtils.debug(log, JsonUtil.toJsonString(execute));
        } catch (Exception e) {
            log.error("发送lark消息失败", e);
            return false;
        }

        return true;
    }

    private List buildElements(final String text) {
        Map<String, String> map = new HashMap<>();
        map.put("tag", "markdown");
        map.put("content", text);
        return Collections.singletonList(map);
    }

    private Map<String, Object> buildHeader(final String title) {
        Map<String, Object> map = new HashMap<>();
        map.put("template", "red");

        Map<String, String> titleMap = new HashMap<>();
        titleMap.put("content", title);
        titleMap.put("tag", "plain_text");

        map.put("title", titleMap);
        return map;
    }

    @Override
    public boolean asyncSendMessage(List<AlarmContext> notifierElements) {

        for (AlarmContext notifierElement : notifierElements) {
            asyncSendMessage(notifierElement);
        }

        return Boolean.TRUE;
    }

    @Data
    @Builder
    private static class LarkMessage {

        @JsonProperty("msg_type")
        private String msgType;

        private Map<String, Object> card;
    }

}

