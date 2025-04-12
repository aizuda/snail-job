package com.aizuda.snailjob.common.core.alarm.strategy;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.attribute.QiYeWechatAttribute;
import com.aizuda.snailjob.common.core.enums.AlarmTypeEnum;
import com.aizuda.snailjob.common.core.util.DingDingUtils;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * 企业微信通知
 *
 * @author lizhongyuan
 */
@Slf4j
@Component
public class QiYeWechatAlarm extends AbstractAlarm<AlarmContext> {

    public static final String AT_LABEL = "<@{0}>";

    @Override
    public Integer getAlarmType() {
        return AlarmTypeEnum.WE_COM.getValue();
    }

    @Override
    public boolean syncSendMessage(AlarmContext context) {
        try {
            QiYeWechatAttribute qiYeWechatAttribute = JsonUtil.parseObject(context.getNotifyAttribute(), QiYeWechatAttribute.class);
            String webhookUrl = qiYeWechatAttribute.getWebhookUrl();
            if (StrUtil.isBlank(webhookUrl)) {
                log.error("Please configure the WeChat robot webhookUrl first");
                return false;
            }
            Map<String, Object> map = MapUtil.newHashMap();
            QiYeWechatMessageContent messageContent = new QiYeWechatMessageContent();
            messageContent.setContent(StrUtil.sub(DingDingUtils.getAtText(qiYeWechatAttribute.getAts(), context.getText(), AT_LABEL), 0, 4096));
            map.put("msgtype", "markdown");
            map.put("markdown", messageContent);
            HttpRequest post = HttpUtil.createPost(webhookUrl);
            HttpRequest request = post.body(JsonUtil.toJsonString(map), ContentType.JSON.toString());
            HttpResponse execute = request.execute();
            SnailJobLog.LOCAL.debug(JsonUtil.toJsonString(execute));
            if (execute.isOk()) {
                return true;
            }
            SnailJobLog.LOCAL.error("Sending Enterprise WeChat message failed: {}", execute.body());
            return false;
        } catch (Exception e) {
            log.error("Sending Enterprise WeChat message failed", e);
            return false;
        }
    }

    @Override
    public boolean asyncSendMessage(List<AlarmContext> alarmContexts) {

        for (AlarmContext alarmContext : alarmContexts) {
            asyncSendMessage(alarmContext);
        }

        return Boolean.TRUE;
    }


    @Data
    private static class QiYeWechatMessageContent {
        /**
         * markdown内容，最长不超过4096个字节，必须是utf8编码
         */
        private String content;
    }

}

