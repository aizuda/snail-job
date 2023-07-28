package com.aizuda.easy.retry.server.support.dispatch.actor.exec;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.model.RetryCallbackDTO;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import com.aizuda.easy.retry.server.client.RequestBuilder;
import com.aizuda.easy.retry.server.client.RpcClient;
import com.aizuda.easy.retry.server.config.RequestDataHelper;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.support.IdempotentStrategy;
import com.aizuda.easy.retry.server.support.context.CallbackRetryContext;
import com.aizuda.easy.retry.server.support.dispatch.actor.log.RetryTaskLogDTO;
import com.aizuda.easy.retry.server.support.handler.CallbackRetryTaskHandler;
import com.aizuda.easy.retry.server.support.retry.RetryExecutor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
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

    @Autowired
    @Qualifier("bitSetIdempotentStrategyHandler")
    private IdempotentStrategy<String, Integer> idempotentStrategy;
    @Autowired
    private RetryTaskMapper retryTaskMapper;
    @Autowired
    private CallbackRetryTaskHandler callbackRetryTaskHandler;

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

                        String message = "回调客户端成功";
                        if (StatusEnum.YES.getStatus() != result.getStatus()) {
                            if (StringUtils.isNotBlank(result.getMessage())) {
                                message = result.getMessage();
                            } else {
                                message = "回调客户端失败: 异常信息为空";
                            }
                        }
                        retryTaskLog.setMessage(message);
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
     * @param callbackTask {@link RetryTask} 回调任务
     * @return 重试结果返回值
     */
    private Result callClient(RetryTask callbackTask, RegisterNodeInfo serverNode) {

        String retryTaskUniqueId = callbackRetryTaskHandler.getRetryTaskUniqueId(callbackTask.getUniqueId());
        RequestDataHelper.setPartition(callbackTask.getGroupName());
        RetryTask retryTask = retryTaskMapper.selectOne(
            new LambdaQueryWrapper<RetryTask>().eq(RetryTask::getUniqueId, retryTaskUniqueId));
        Assert.notNull(retryTask, () -> new EasyRetryServerException("未查询回调任务对应的重试任务. callbackUniqueId:[{}] uniqueId:[{}]",
            callbackTask.getUniqueId(), retryTaskUniqueId));

        // 回调参数
        RetryCallbackDTO retryCallbackDTO = new RetryCallbackDTO();
        retryCallbackDTO.setIdempotentId(retryTask.getIdempotentId());
        retryCallbackDTO.setRetryStatus(retryTask.getRetryStatus());
        retryCallbackDTO.setArgsStr(retryTask.getArgsStr());
        retryCallbackDTO.setScene(retryTask.getSceneName());
        retryCallbackDTO.setGroup(retryTask.getGroupName());
        retryCallbackDTO.setExecutorName(retryTask.getExecutorName());
        retryCallbackDTO.setUniqueId(retryTask.getUniqueId());

        RpcClient rpcClient = RequestBuilder.<RpcClient, Result>newBuilder()
            .hostPort(serverNode.getHostPort())
            .groupName(serverNode.getGroupName())
            .hostId(serverNode.getHostId())
            .hostIp(serverNode.getHostIp())
            .contextPath(serverNode.getContextPath())
            .client(RpcClient.class)
            .build();

        return rpcClient.callback(retryCallbackDTO);

    }


}
