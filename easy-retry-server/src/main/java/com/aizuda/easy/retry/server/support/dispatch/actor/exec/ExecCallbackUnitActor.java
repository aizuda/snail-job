package com.aizuda.easy.retry.server.support.dispatch.actor.exec;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.aizuda.easy.retry.client.model.RetryCallbackDTO;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.EasyRetryHeaders;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.support.IdempotentStrategy;
import com.aizuda.easy.retry.server.support.context.CallbackRetryContext;
import com.aizuda.easy.retry.server.support.dispatch.actor.log.RetryTaskLogDTO;
import com.aizuda.easy.retry.server.support.retry.RetryExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 重试结果执行器
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 1.5.0
 */
@Component(ExecCallbackUnitActor.BEAN_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ExecCallbackUnitActor extends AbstractActor  {

    public static final String BEAN_NAME = "ExecCallbackUnitActor";
    public static final String URL = "http://{0}:{1}/{2}/retry/callback/v1";

    @Autowired
    @Qualifier("bitSetIdempotentStrategyHandler")
    private IdempotentStrategy<String, Integer> idempotentStrategy;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryExecutor.class, retryExecutor -> {

            CallbackRetryContext context = (CallbackRetryContext) retryExecutor.getRetryContext();
            RetryTask retryTask = context.getRetryTask();
            RegisterNodeInfo serverNode = context.getServerNode();

            RetryTaskLogDTO retryTaskLog = new RetryTaskLogDTO();
            retryTaskLog.setGroupName(retryTask.getGroupName());
            retryTaskLog.setUniqueId(retryTask.getUniqueId());
            retryTaskLog.setRetryStatus(retryTask.getRetryStatus());
            try {

                if (Objects.nonNull(serverNode)) {
                    retryExecutor.call((Callable<Result<Void>>) () -> {
                        Result<Void> result = callClient(retryTask, serverNode);

                        if (StatusEnum.YES.getStatus() != result.getStatus()  && StringUtils.isNotBlank(result.getMessage())) {
                            retryTaskLog.setMessage(result.getMessage());
                        } else {
                            retryTaskLog.setMessage("调度成功");
                        }

                        return result;
                    });
                    if (context.hasException()) {
                        retryTaskLog.setMessage(context.getException().getMessage());
                    }
                } else {
                    retryTaskLog.setMessage("There are currently no available client PODs.");
                }

            }catch (Exception e) {
                LogUtils.error(log, "callback client error. retryTask:[{}]", JsonUtil.toJsonString(retryTask), e);
                retryTaskLog.setMessage(StringUtils.isBlank(e.getMessage()) ? StringUtils.EMPTY : e.getMessage());
            } finally {

                // 清除幂等标识位
                idempotentStrategy.clear(retryTask.getGroupName(), retryTask.getId().intValue());

                ActorRef actorRef = ActorGenerator.logActor();
                actorRef.tell(retryTaskLog, actorRef);

                getContext().stop(getSelf());

            }

        }).build();
    }

    /**
     * 调用客户端
     *
     * @param retryTask {@link RetryTask} 需要重试的数据
     * @return 重试结果返回值
     */
    private Result<Void> callClient(RetryTask retryTask, RegisterNodeInfo serverNode) {

        // 回调参数
        RetryCallbackDTO retryCallbackDTO = new RetryCallbackDTO();
        retryCallbackDTO.setIdempotentId(retryTask.getIdempotentId());
        retryCallbackDTO.setRetryStatus(retryTask.getRetryStatus());
        retryCallbackDTO.setArgsStr(retryTask.getArgsStr());
        retryCallbackDTO.setScene(retryTask.getSceneName());
        retryCallbackDTO.setGroup(retryTask.getGroupName());
        retryCallbackDTO.setExecutorName(retryTask.getExecutorName());
        retryCallbackDTO.setUniqueId(retryTask.getUniqueId());

        HttpEntity<RetryCallbackDTO> requestEntity = new HttpEntity<>(retryCallbackDTO);

        String format = MessageFormat.format(URL, serverNode.getHostIp(), serverNode.getHostPort().toString(), serverNode.getContextPath());
        Result result = restTemplate.postForObject(format, requestEntity, Result.class);

        LogUtils.info(log, "请求客户端 format:[{}] response:[{}}] ", format, JsonUtil.toJsonString(result));
        return result;

    }


}
