package com.aizuda.snailjob.common.core.alarm.strategy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.attribute.LarkAttribute;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.AlarmTypeEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 飞书通知
 *
 * @author: opensnail
 * @date : 2021-11-25 09:20
 * @since 1.4.0
 */
@Component
@Slf4j
public class LarkAlarm extends AbstractAlarm<AlarmContext> {


    public static final String AT_LABEL = "<at id={0}></at>";

    @Override
    public Integer getAlarmType() {
        return AlarmTypeEnum.LARK.getValue();
    }

    @Override
    public boolean syncSendMessage(AlarmContext context) {
        try {
            LarkAttribute larkAttribute = JsonUtil.parseObject(context.getNotifyAttribute(), LarkAttribute.class);

            Map<String, Object> map = new HashMap<>();
            map.put("header", buildHeader(context.getTitle()));
            map.put("elements", buildElements(context.getText(), larkAttribute.getAts()));

            LarkMessage builder = LarkMessage.builder()
                    .msgType("interactive")
                    .card(map).build();

            HttpRequest post = HttpUtil.createPost(larkAttribute.getWebhookUrl());
            HttpRequest request = post.body(JsonUtil.toJsonString(builder), ContentType.JSON.toString());
            HttpResponse execute = request.execute();
            if (execute.isOk()) {
                return true;
            }
            SnailJobLog.LOCAL.error("发送lark消息失败:{}", execute.body());
            return false;
        } catch (Exception e) {
            log.error("发送lark消息失败", e);
            return false;
        }
    }

    private List buildElements(String text, List<String> ats) {
        Map<String, String> map = new HashMap<>();
        map.put("tag", "markdown");
        map.put("content", getAtText(text, ats));
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

    public String getAtText(String text, List<String> ats) {
        if (CollectionUtils.isEmpty(ats)) {
            return "";
        }

        StringBuilder sb = new StringBuilder(text);
        if (ats.stream().map(String::toLowerCase).anyMatch(SystemConstants.AT_ALL::equals)) {
            sb.append(MessageFormat.format(AT_LABEL, SystemConstants.AT_ALL));
        } else {
            ats.stream().filter(StrUtil::isNotBlank)
                    .forEach(at -> sb.append(MessageFormat.format(AT_LABEL, at)));
        }
        return sb.toString();
    }
}

