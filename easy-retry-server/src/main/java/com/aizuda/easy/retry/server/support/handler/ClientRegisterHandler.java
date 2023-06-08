package com.aizuda.easy.retry.server.support.handler;

import com.aizuda.easy.retry.common.core.enums.HeadersEnum;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * todo 待优化
 * @author www.byteblogs.com
 * @date 2022-03-09
 * @since 2.0
 */
@Component
@Slf4j
public class ClientRegisterHandler {

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 5, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500), r -> new Thread(r, "CLIENT REGISTER THREAD"), (r, executor) -> LogUtils.error(log, "处理注册线程池已经超负荷运作"));
    public static final String URL = "http://{0}:{1}/{2}/retry/sync/version/v1";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    public void syncVersion(Integer clientVersion, String groupName, String hostIp, Integer hostPort, String contextPath) {

        threadPoolExecutor.execute(() -> {
            try {
                Integer serverVersion = configAccess.getConfigVersion(groupName);
                if (Objects.isNull(clientVersion) || !clientVersion.equals(serverVersion)) {
                    LogUtils.info(log,"客户端[{}]:[{}] 组:[{}] 版本号不一致clientVersion:[{}] serverVersion:[{}]",
                            hostIp, hostPort, clientVersion, serverVersion);
                    // 同步
                    ConfigDTO configDTO = configAccess.getConfigInfo(groupName);
                    String format = MessageFormat.format(URL, hostIp, hostPort.toString(), contextPath);
                    Result result = restTemplate.postForObject(format, configDTO, Result.class);
                    LogUtils.info(log,"同步结果 [{}]", result);

                }

            } catch (Exception e) {
                LogUtils.error(log,"同步版本失败", e);
            }
        });

    }
}
