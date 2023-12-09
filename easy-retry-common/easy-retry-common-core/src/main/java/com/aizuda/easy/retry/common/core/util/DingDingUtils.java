package com.aizuda.easy.retry.common.core.util;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-25 09:54
 */
@Slf4j
public class DingDingUtils {

    public static final String atLabel = "@{0}";

    /**
     * 防止文本过长钉钉限流，目前最大为4000
     *
     * @param text
     * @return
     */
    private static String subTextLength(String text) {
        int length = text.length();

        if (length > 4000) {
            return text.substring(0, 4000);
        } else {
            return text;
        }
    }

    /**
     * 组装OapiRobotSendRequest
     *
     * @param title
     * @param text
     * @return
     */
    public static OapiRobotSendRequest buildSendRequest(String title, String text, List<String> ats) {
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle(title);
        request.setMarkdown(markdown);
        markdown.setText(subTextLength(getAtText(ats, text)));

        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setAtMobiles(ats);
        request.setAt(at);
        if (!CollectionUtils.isEmpty(ats)) {
            at.setIsAtAll(ats.stream().map(String::toLowerCase).anyMatch(SystemConstants.AT_ALL::equals));
        }
        return request;
    }

    public static String getAtText(List<String> ats, String text) {
        if (CollectionUtils.isEmpty(ats)) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        ats.stream().filter(StrUtil::isNotBlank)
                .forEach(at -> sb.append(MessageFormat.format(atLabel, at)));
        return sb.toString();
    }

    /**
     * @param request
     */
    public static boolean sendMessage(OapiRobotSendRequest request, String url) {

        try {
            if (StrUtil.isNotBlank(url)) {
                return false;
            }
            DingTalkClient client = new DefaultDingTalkClient(url);
            client.execute(request);

            return true;
        } catch (Exception e) {
            LogUtils.error(log, "dingDingProcessNotify", e);
        }

        return false;
    }

}
