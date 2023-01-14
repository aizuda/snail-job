package com.x.retry.server.support.dispatch.actor.exec;

import akka.actor.AbstractActor;
import com.x.retry.client.model.DispatchRetryDTO;
import com.x.retry.client.model.DispatchRetryResultDTO;
import com.x.retry.common.core.constant.SystemConstants;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.model.Result;
import com.x.retry.common.core.model.XRetryHeaders;
import com.x.retry.common.core.util.Assert;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.server.exception.XRetryServerException;
import com.x.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import com.x.retry.server.support.IdempotentStrategy;
import com.x.retry.server.support.context.MaxAttemptsPersistenceRetryContext;
import com.x.retry.server.support.retry.RetryExecutor;
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
public class ExecUnitActor extends AbstractActor  {

    public static final String BEAN_NAME = "ExecUnitActor";
    public static final String URL = "http://{0}:{1}/retry/dispatch/v1";

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

            RetryTaskLog retryTaskLog = new RetryTaskLog();
            retryTaskLog.setErrorMessage(StringUtils.EMPTY);

            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryExecutor.getRetryContext();
            RetryTask retryTask = context.getRetryTask();
            ServerNode serverNode = context.getServerNode();

            try {

                if (Objects.nonNull(serverNode)) {
                    Object call = retryExecutor.call((Callable<Result<DispatchRetryResultDTO>>) () -> callClient(retryTask, retryTaskLog, serverNode));
                } else {
                    retryTaskLog.setErrorMessage("暂无可用的客户端POD");
                }

            }catch (Exception e) {
                LogUtils.error("回调客户端失败 retryTask:[{}]", JsonUtil.toJsonString(retryTask), e);
                retryTaskLog.setErrorMessage(StringUtils.isBlank(e.getMessage()) ? StringUtils.EMPTY : e.getMessage());
            } finally {

                // 清除幂等标识位
                idempotentStrategy.clear(retryTask.getGroupName(), retryTask.getId().intValue());
                getContext().stop(getSelf());

                // 记录重试日志
                BeanUtils.copyProperties(retryTask, retryTaskLog);
                retryTaskLog.setCreateDt(LocalDateTime.now());
                retryTaskLog.setId(null);
                Assert.isTrue(1 ==  retryTaskLogMapper.insert(retryTaskLog),
                        new XRetryServerException("新增重试日志失败"));
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
        dispatchRetryDTO.setBizId(retryTask.getBizId());
        dispatchRetryDTO.setScene(retryTask.getSceneName());
        dispatchRetryDTO.setExecutorName(retryTask.getExecutorName());
        dispatchRetryDTO.setArgsStr(retryTask.getArgsStr());

        // 设置header
        HttpHeaders requestHeaders = new HttpHeaders();
        XRetryHeaders xRetryHeaders = new XRetryHeaders();
        xRetryHeaders.setXRetry(Boolean.TRUE);
        requestHeaders.add(SystemConstants.X_RETRY_HEAD_KEY, JsonUtil.toJsonString(xRetryHeaders));

        HttpEntity<DispatchRetryDTO> requestEntity = new HttpEntity<>(dispatchRetryDTO, requestHeaders);

        String format = MessageFormat.format(URL, serverNode.getHostIp(), serverNode.getHostPort().toString());
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

        LogUtils.info("请求客户端 response:[{}}] ", JsonUtil.toJsonString(result));
        return result;

    }


}
