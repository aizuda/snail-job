package com.x.retry.common.core.util;

import com.x.retry.common.core.log.LogUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-25 09:54
 */
@Slf4j
public class DingDingUtils {
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
    public static OapiRobotSendRequest buildSendRequest(String title, String text) {

        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("markdown");
        OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
        markdown.setTitle(title);
        markdown.setText(subTextLength(text));
        request.setMarkdown(markdown);

        return request;
    }

    /**
     *
     * @param request
     */
    public static boolean sendMessage(OapiRobotSendRequest request, String url){

        try {
            if (StringUtils.isEmpty(url)) {
                return false;
            }
            DingTalkClient client = new DefaultDingTalkClient(url);
            client.execute(request);

            return true;
        } catch (Exception e) {
            LogUtils.error(log,"dingDingProcessNotify", e);
        }

        return false;
    }

}
