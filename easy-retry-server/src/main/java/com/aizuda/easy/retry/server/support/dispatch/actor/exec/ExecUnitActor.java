package com.aizuda.easy.retry.server.support.dispatch.actor.exec;

import akka.actor.AbstractActor;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.model.DispatchRetryDTO;
import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.model.EasyRetryHeaders;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.service.convert.RetryTaskLogConverter;
import com.aizuda.easy.retry.server.support.IdempotentStrategy;
import com.aizuda.easy.retry.server.support.context.MaxAttemptsPersistenceRetryContext;
import com.aizuda.easy.retry.server.support.retry.RetryExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 重试结果执行器
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component("ExecUnitActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ExecUnitActor extends AbstractActor  {

    public static final String BEAN_NAME = "ExecUnitActor";
    public static final String URL = "http://{0}:{1}/{2}/retry/dispatch/v1";

    @Autowired
    @Qualifier("bitSetIdempotentStrategyHandler")
    private IdempotentStrategy<String, Integer> idempotentStrategy;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryExecutor.class, retryExecutor -> {

            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryExecutor.getRetryContext();
            RetryTask retryTask = context.getRetryTask();
            ServerNode serverNode = context.getServerNode();

            RetryTaskLog retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTask(retryTask);
            retryTaskLog.setErrorMessage(StringUtils.EMPTY);

            try {

                if (Objects.nonNull(serverNode)) {
                    retryExecutor.call((Callable<Result<DispatchRetryResultDTO>>) () -> callClient(retryTask, retryTaskLog, serverNode));
                    if (context.hasException()) {
                        retryTaskLog.setErrorMessage(context.getException().getMessage());
                    }
                } else {
                    retryTaskLog.setErrorMessage("暂无可用的客户端POD");
                }

            }catch (Exception e) {
                LogUtils.error(log, "回调客户端失败 retryTask:[{}]", JsonUtil.toJsonString(retryTask), e);
                retryTaskLog.setErrorMessage(StringUtils.isBlank(e.getMessage()) ? StringUtils.EMPTY : e.getMessage());
            } finally {

                // 清除幂等标识位
                idempotentStrategy.clear(retryTask.getGroupName(), retryTask.getId().intValue());
                getContext().stop(getSelf());

                // 记录重试日志
                retryTaskLog.setCreateDt(LocalDateTime.now());
                retryTaskLog.setId(null);
                Assert.isTrue(1 ==  retryTaskLogMapper.insert(retryTaskLog),
                    () -> new EasyRetryServerException("新增重试日志失败"));
            }

        }).build();
    }

    /**
     * 调用客户端
     *
     * @param retryTask {@link RetryTask} 需要重试的数据
     * @return 重试结果返回值
     */
    private Result<DispatchRetryResultDTO> callClient(RetryTask retryTask, RetryTaskLog retryTaskLog, ServerNode serverNode) {

        DispatchRetryDTO dispatchRetryDTO = new DispatchRetryDTO();
        dispatchRetryDTO.setIdempotentId(retryTask.getIdempotentId());
        dispatchRetryDTO.setScene(retryTask.getSceneName());
        dispatchRetryDTO.setExecutorName(retryTask.getExecutorName());
        dispatchRetryDTO.setArgsStr(retryTask.getArgsStr());
        dispatchRetryDTO.setUniqueId(retryTask.getUniqueId());

        // 设置header
        HttpHeaders requestHeaders = new HttpHeaders();
        EasyRetryHeaders easyRetryHeaders = new EasyRetryHeaders();
        easyRetryHeaders.setEasyRetry(Boolean.TRUE);
        easyRetryHeaders.setEasyRetryId(retryTask.getUniqueId());
        requestHeaders.add(SystemConstants.EASY_RETRY_HEAD_KEY, JsonUtil.toJsonString(easyRetryHeaders));

        HttpEntity<DispatchRetryDTO> requestEntity = new HttpEntity<>(dispatchRetryDTO, requestHeaders);

        String format = MessageFormat.format(URL, serverNode.getHostIp(), serverNode.getHostPort().toString(), serverNode.getContextPath());
        Result result = restTemplate.postForObject(format, requestEntity, Result.class);

        if (1 != result.getStatus()  && StringUtils.isNotBlank(result.getMessage())) {
            retryTaskLog.setErrorMessage(result.getMessage());
        } else {
            DispatchRetryResultDTO data = JsonUtil.parseObject(JsonUtil.toJsonString(result.getData()), DispatchRetryResultDTO.class);
            result.setData(data);
            if (Objects.nonNull(data) && StringUtils.isNotBlank(data.getExceptionMsg())) {
                retryTaskLog.setErrorMessage(data.getExceptionMsg());
            }

        }

        LogUtils.info(log, "请求客户端 response:[{}}] ", JsonUtil.toJsonString(result));
        return result;

    }


}
