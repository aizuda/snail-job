package com.aizuda.snailjob.server.common.rpc.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Register;
import com.aizuda.snailjob.server.common.handler.GetHttpRequestHandler;
import com.aizuda.snailjob.server.common.register.ClientRegister;
import com.aizuda.snailjob.server.common.register.RegisterContext;
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
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Beat check content:[{}]", content);
        Register register = SnailSpringContext.getBean(ClientRegister.BEAN_NAME, Register.class);
        RegisterContext registerContext = new RegisterContext();
        registerContext.setGroupName(headers.get(HeadersEnum.GROUP_NAME.getKey()));
        registerContext.setHostPort(Integer.valueOf(headers.get(HeadersEnum.HOST_PORT.getKey())));
        registerContext.setHostIp(headers.get(HeadersEnum.HOST_IP.getKey()));
        registerContext.setHostId(headers.get(HeadersEnum.HOST_ID.getKey()));
        registerContext.setUri(HTTP_PATH.BEAT);
        registerContext.setNamespaceId(headers.get(HeadersEnum.NAMESPACE.getKey()));
        registerContext.setLabels(headers.get(HeadersEnum.LABEL.getKey()));
        boolean result = register.register(registerContext);
        if (!result) {
            SnailJobLog.LOCAL.warn("client register error. groupName:[{}]", headers.get(HeadersEnum.GROUP_NAME.getKey()));
        }
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        return new SnailJobRpcResult(PONG, retryRequest.getReqId());
    }
}
