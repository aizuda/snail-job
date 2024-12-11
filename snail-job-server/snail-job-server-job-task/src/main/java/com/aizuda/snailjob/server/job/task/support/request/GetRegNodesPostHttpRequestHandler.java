package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.cache.CacheConsumerGroup;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.handler.GetHttpRequestHandler;
import com.aizuda.snailjob.server.common.register.ClientRegister;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.GET_REG_NODES_AND_REFRESH;
import static com.aizuda.snailjob.server.common.register.ClientRegister.DELAY_TIME;

/**
 * 获取服务端缓存的客户端节点 并刷新本地时间
 *
 */
@Component
public class GetRegNodesPostHttpRequestHandler extends GetHttpRequestHandler {

    @Override
    public boolean supports(String path) {
        return GET_REG_NODES_AND_REFRESH.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Client Callback Request. content:[{}]", content);

        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);


        List<ServerNode> refreshCache = getAndRefreshCache();
        String json = null;
        if (CollUtil.isNotEmpty(refreshCache)){
            json = JsonUtil.toJsonString(refreshCache);
        }
        return new SnailJobRpcResult(json, retryRequest.getReqId());
    }

    public static List<ServerNode> getAndRefreshCache() {
        // 获取当前所有需要续签的node
        List<ServerNode> expireNodes = ClientRegister.getExpireNodes();
        if (Objects.nonNull(expireNodes)) {
            // 进行本地续签
            for (final ServerNode serverNode : expireNodes) {
                serverNode.setExpireAt(LocalDateTime.now().plusSeconds(DELAY_TIME));
                // 刷新全量本地缓存
                CacheRegisterTable.addOrUpdate(serverNode);
                // 刷新过期时间
                CacheConsumerGroup.addOrUpdate(serverNode.getGroupName(), serverNode.getNamespaceId());
            }
        }
        return expireNodes;
    }


}
