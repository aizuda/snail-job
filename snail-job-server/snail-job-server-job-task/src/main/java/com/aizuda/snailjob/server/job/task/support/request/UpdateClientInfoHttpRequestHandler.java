package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.cache.CacheConsumerGroup;
import com.aizuda.snailjob.server.common.convert.RegisterNodeInfoConverter;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.register.ClientRegister;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.UPDATE_CLIENT_INFO;
import static com.aizuda.snailjob.server.common.register.ClientRegister.DELAY_TIME;

/**
 * 刷新客户端信息
 *
 */
@Component
@RequiredArgsConstructor
public class UpdateClientInfoHttpRequestHandler extends PostHttpRequestHandler {
    private final InstanceManager instanceManager;
    @Override
    public boolean supports(String path) {
        return UPDATE_CLIENT_INFO.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Client Update Request. content:[{}]", content);

        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        ServerNode serverNode = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), ServerNode.class);

        instanceManager.updateInstanceLabels(serverNode);
        return new SnailJobRpcResult("success", retryRequest.getReqId());
    }

    public  List<ServerNode> getAndRefreshCache() {
        // 获取当前所有需要续签的node
        List<ServerNode> expireNodes = ClientRegister.getExpireNodes();
        if (Objects.nonNull(expireNodes)) {
            // 进行本地续签
            for (final ServerNode serverNode : expireNodes) {
                serverNode.setExpireAt(LocalDateTime.now().plusSeconds(DELAY_TIME));
                // 刷新全量本地缓存
                instanceManager.registerOrUpdate(RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(serverNode));
                // 刷新过期时间
                CacheConsumerGroup.addOrUpdate(serverNode.getGroupName(), serverNode.getNamespaceId());
            }
        }
        return expireNodes;
    }


}
