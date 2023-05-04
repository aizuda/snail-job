package com.aizuda.easy.retry.server.support.dispatch.actor.callback;

import akka.actor.AbstractActor;
import cn.hutool.core.util.IdUtil;
import com.aizuda.easy.retry.client.model.RetryCallbackDTO;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.model.EasyRetryHeaders;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.support.handler.ClientNodeAllocateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2023-01-10 08:50
 */
@Component("CallbackRetryResultActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class CallbackRetryResultActor extends AbstractActor {

    public static final String BEAN_NAME = "CallbackRetryResultActor";
    public static final String URL = "http://{0}:{1}/{2}/retry/callback/v1";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ClientNodeAllocateHandler clientNodeAllocateHandler;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTask.class, retryTask->{

            try {
                ServerNode serverNode = clientNodeAllocateHandler.getServerNode(retryTask.getGroupName());
                if (Objects.isNull(serverNode)) {
                    LogUtils.warn(log, "暂无可用的客户端节点");
                    return;
                }

                // 回调参数
                RetryCallbackDTO retryCallbackDTO = new RetryCallbackDTO();
                retryCallbackDTO.setIdempotentId(retryTask.getIdempotentId());
                retryCallbackDTO.setRetryStatus(retryTask.getRetryStatus());
                retryCallbackDTO.setArgsStr(retryTask.getArgsStr());
                retryCallbackDTO.setScene(retryTask.getSceneName());
                retryCallbackDTO.setGroup(retryTask.getGroupName());
                retryCallbackDTO.setExecutorName(retryTask.getExecutorName());

                // 设置header
                HttpHeaders requestHeaders = new HttpHeaders();
                EasyRetryHeaders easyRetryHeaders = new EasyRetryHeaders();
                easyRetryHeaders.setEasyRetry(Boolean.TRUE);
                easyRetryHeaders.setEasyRetryId(IdUtil.simpleUUID());
                requestHeaders.add(SystemConstants.EASY_RETRY_HEAD_KEY, JsonUtil.toJsonString(easyRetryHeaders));

                HttpEntity<RetryCallbackDTO> requestEntity = new HttpEntity<>(retryCallbackDTO, requestHeaders);

                String format = MessageFormat.format(URL, serverNode.getHostIp(), serverNode.getHostPort().toString(), serverNode.getContextPath());
                Result result = restTemplate.postForObject(format, requestEntity, Result.class);
                LogUtils.info(log, "回调请求客户端 response:[{}}] ", JsonUtil.toJsonString(result));

            } finally {
                getContext().stop(getSelf());
            }

        }).build();
    }


}
