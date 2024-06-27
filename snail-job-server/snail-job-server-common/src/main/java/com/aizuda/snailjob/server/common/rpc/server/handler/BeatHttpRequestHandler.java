package com.aizuda.snailjob.server.common.rpc.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.handler.GetHttpRequestHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.BEAT.PONG;

/**
 * 接收心跳请求
 *
 * @author: opensnail
 * @date : 2022-03-07 16:26
 * @since 1.0.0
 */
@Component
public class BeatHttpRequestHandler extends GetHttpRequestHandler {

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.BEAT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public String doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Beat check content:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        return JsonUtil.toJsonString(new NettyResult(PONG, retryRequest.getReqId()));
    }
}
