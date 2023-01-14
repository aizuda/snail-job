package com.x.retry.server.support.handler;

import com.x.retry.common.core.enums.HeadersEnum;
import com.x.retry.common.core.enums.NodeTypeEnum;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.model.Result;
import com.x.retry.server.model.dto.ConfigDTO;
import com.x.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import com.x.retry.server.persistence.support.ConfigAccess;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author www.byteblogs.com
 * @date 2022-03-09
 * @since 2.0
 */
@Component
public class ClientRegisterHandler {

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 5, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500), r -> new Thread(r, "CLIENT REGISTER THREAD"), (r, executor) -> LogUtils.error("处理注册线程池已经超负荷运作"));
    public static final String URL = "http://{0}:{1}/retry/sync/version/v1";

    @Autowired
    private ServerNodeMapper serverNodeMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    public void registerClient(HttpHeaders headers) {

        threadPoolExecutor.execute(() -> {

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

    public void syncVersion(Integer clientVersion, String groupName, String hostIp, Integer hostPort) {

        threadPoolExecutor.execute(() -> {
            try {
                Integer serverVersion = configAccess.getConfigVersion(groupName);
                if (Objects.isNull(clientVersion) || !clientVersion.equals(serverVersion)) {
                    LogUtils.info("客户端[{}]:[{}] 组:[{}] 版本号不一致clientVersion:[{}] serverVersion:[{}]",
                            hostIp, hostPort, clientVersion, serverVersion);
                    // 同步
                    ConfigDTO configDTO = configAccess.getConfigInfo(groupName);
                    String format = MessageFormat.format(URL, hostIp, hostPort.toString());
                    Result result = restTemplate.postForObject(format, configDTO, Result.class);
                    LogUtils.info("同步结果 [{}]", result);

                }

            } catch (Exception e) {
                LogUtils.error("同步版本失败", e);
            }
        });

    }
}
