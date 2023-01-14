package com.x.retry.server.support.handler;

import com.x.retry.common.core.enums.HeadersEnum;
import com.x.retry.common.core.enums.NodeTypeEnum;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * @author www.byteblogs.com
 * @date 2022-03-09
 * @since 2.0
 */
@Component
public class ClientRegisterHandler {

    @Autowired
    private ServerNodeMapper serverNodeMapper;

    public void registerClient(HttpHeaders headers) {

        CompletableFuture.runAsync(() -> {

            String hostId = headers.get(HeadersEnum.HOST_ID.getKey());
            String hostIp = headers.get(HeadersEnum.HOST_IP.getKey());
            Integer hostPort = headers.getInt(HeadersEnum.HOST_PORT.getKey());
            String groupName = headers.get(HeadersEnum.GROUP_NAME.getKey());

            LocalDateTime endTime = LocalDateTime.now().plusSeconds(30);
            ServerNode serverNode = new ServerNode();
            serverNode.setGroupName(groupName);
            serverNode.setNodeType(NodeTypeEnum.CLIENT.getType());
            serverNode.setHostPort(hostPort);
            serverNode.setHostIp(hostIp);
            serverNode.setExpireAt(endTime);
            serverNode.setCreateDt(LocalDateTime.now());
            serverNode.setHostId(hostId);

            try {
                int i = serverNodeMapper.insertOrUpdate(serverNode);
            } catch (Exception e) {
                LogUtils.error("注册客户端失败", e);
            }
        });

    }

}
