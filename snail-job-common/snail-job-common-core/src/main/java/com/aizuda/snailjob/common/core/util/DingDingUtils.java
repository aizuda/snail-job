package com.aizuda.snailjob.common.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: opensnail
 * @date : 2021-11-25 09:54
 */
@Slf4j
public class DingDingUtils {

    public static final String atLabel = "@{0}";

    /**
     * 组装 DingTalkRequest
     *
     * @param title 标题
     * @param text  内容
     * @return DingTalkRequest
     */
    public static String buildSendRequest(String title, String text, List<String> ats) {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "markdown");
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("text", StrUtil.sub(getAtText(ats, text, atLabel), 0, 4000));
        markdown.put("title", title);
        message.put("markdown", markdown);

        // 处理提到的人
        Map<String, Object> at = new HashMap<>();
        at.put("atMobiles", ats);
        at.put("isAtAll", true);
        if (CollUtil.isNotEmpty(ats)) {
            at.put("isAtAll", ats.stream().map(String::toLowerCase).anyMatch(SystemConstants.AT_ALL::equals));
        }
        message.put("at", at);
        return JsonUtil.toJsonString(message);
    }

    public static String getAtText(List<String> ats, String text, String atLabel) {
        if (CollUtil.isEmpty(ats)) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        sb.append(StrUtil.CRLF);
        ats.stream().filter(StrUtil::isNotBlank)
                .forEach(at -> sb.append(MessageFormat.format(atLabel, at)));
        return sb.toString();
    }

    /**
     * @param request DingTalkRequest
     */
    public static boolean sendMessage(String request, String url) {

        try {
            if (StrUtil.isBlank(url)) {
                return false;
            }

            // 发送POST请求
            HttpResponse response = HttpRequest.post(url)
                    .headerMap(getHeaders(), true)
                    .body(request)
                    .execute();

            String body = response.body();
            JsonNode bodyJson = JsonUtil.toJson(body);
            int errCode = bodyJson.get("errcode").asInt();
            if (errCode != 0) {
                SnailJobLog.LOCAL.error("dingDingProcessNotify: 钉钉发送失败, 错误码:{}, 错误信息:{}", errCode, bodyJson.get("errmsg").asText());
                return false;
            }
            return true;
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("dingDingProcessNotify", e);
        }

        return false;
    }

    public static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

}
