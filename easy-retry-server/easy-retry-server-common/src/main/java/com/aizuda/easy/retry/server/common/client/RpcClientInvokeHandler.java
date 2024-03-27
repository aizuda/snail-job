package com.aizuda.easy.retry.server.common.client;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.NetUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.client.annotation.Body;
import com.aizuda.easy.retry.server.common.client.annotation.Header;
import com.aizuda.easy.retry.server.common.client.annotation.Mapping;
import com.aizuda.easy.retry.server.common.client.annotation.Param;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 请求处理器
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-11 21:45
 * @since 2.0.0
 */
@Slf4j
public class RpcClientInvokeHandler implements InvocationHandler {

    private final String groupName;
    private String hostId;
    private String hostIp;
    private Integer hostPort;
    private String contextPath;
    private final boolean failRetry;
    private final int retryTimes;
    private final int retryInterval;
    private final RetryListener retryListener;
    private final boolean failover;
    private final Integer routeKey;
    private final String allocKey;
    private final Integer executorTimeout;

    private final String namespaceId;

    public RpcClientInvokeHandler(final String groupName, final RegisterNodeInfo registerNodeInfo,
        final boolean failRetry, final int retryTimes,
        final int retryInterval, final RetryListener retryListener, final Integer routeKey, final String allocKey,
        final boolean failover, final Integer executorTimeout, final String namespaceId) {
        this.groupName = groupName;
        this.hostId = registerNodeInfo.getHostId();
        this.hostPort = registerNodeInfo.getHostPort();
        this.hostIp = registerNodeInfo.getHostIp();
        this.contextPath = registerNodeInfo.getContextPath();
        this.failRetry = failRetry;
        this.retryTimes = retryTimes;
        this.retryInterval = retryInterval;
        this.retryListener = retryListener;
        this.failover = failover;
        this.routeKey = routeKey;
        this.allocKey = allocKey;
        this.executorTimeout = executorTimeout;
        this.namespaceId = namespaceId;
    }

    @Override
    public Result invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Mapping annotation = method.getAnnotation(Mapping.class);
        Assert.notNull(annotation, () -> new EasyRetryServerException("@Mapping cannot be null"));

        if (failover) {
            return doFailoverHandler(method, args, annotation);
        }

        return requestRemote(method, args, annotation, 1);
    }

    @NotNull
    private Result doFailoverHandler(final Method method, final Object[] args, final Mapping annotation)
        throws Throwable {
        Set<RegisterNodeInfo> serverNodeSet = CacheRegisterTable.getServerNodeSet(groupName, namespaceId);

        // 最多调用size次
        int size = serverNodeSet.size();
        for (int count = 1; count <= size; count++) {
            log.debug("Start request client. count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count, hostId,
                hostIp, hostPort, NetUtil.getLocalIpStr());
            Result result = requestRemote(method, args, annotation, count);
            if (Objects.nonNull(result)) {
                return result;
            }
        }

        throw new EasyRetryServerException("No available nodes.");
    }

    private Result requestRemote(Method method, Object[] args, Mapping mapping, int count) throws Throwable {

        try {

            // 参数解析
            ParseParasResult parasResult = doParseParams(method, args);

            // 若是POST请求，请求体不能是null
            if (RequestMethod.POST.name().equals(mapping.method().name())) {
                Assert.notNull(parasResult.body, () -> new EasyRetryServerException("body cannot be null"));
            }

            RestTemplate restTemplate = SpringContext.getBean(RestTemplate.class);

            Retryer<Result> retryer = buildResultRetryer();

            HttpHeaders requestHeaders = parasResult.requestHeaders;
            if (Objects.nonNull(executorTimeout)) {
                requestHeaders.set(RequestInterceptor.TIMEOUT_TIME, String.valueOf(executorTimeout));
            }

            Result result = retryer.call(() -> {
                ResponseEntity<Result> response = restTemplate.exchange(
                    // 拼接 url?a=1&b=1
                    getUrl(mapping, parasResult.paramMap).toString(),
                    // post or get
                    HttpMethod.valueOf(mapping.method().name()),
                    // body
                    new HttpEntity<>(parasResult.body, parasResult.requestHeaders),
                    // 返回值类型
                    Result.class);

                return Objects.requireNonNull(response.getBody());
            });

            log.debug("Request client success. count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count, hostId,
                hostIp, hostPort, NetUtil.getLocalIpStr());

            return result;
        } catch (RestClientException ex) {
            // 网络异常
            if (ex instanceof ResourceAccessException && failover) {
                log.error("request client I/O error, count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count,
                    hostId, hostIp, hostPort, NetUtil.getLocalIpStr(), ex);

                // 进行路由剔除处理
                CacheRegisterTable.remove(groupName, namespaceId, hostId);
                // 重新选一个可用的客户端节点
                ClientNodeAllocateHandler clientNodeAllocateHandler = SpringContext.getBean(
                    ClientNodeAllocateHandler.class);
                RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(allocKey, groupName, namespaceId,
                    routeKey);
                // 这里表示无可用节点
                if (Objects.isNull(serverNode)) {
                    throw ex;
                }

                this.hostId = serverNode.getHostId();
                this.hostPort = serverNode.getHostPort();
                this.hostIp = serverNode.getHostIp();
                this.contextPath = serverNode.getContextPath();

            } else {
                // 其他异常继续抛出
                log.error("request client error.count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count,
                    hostId, hostIp, hostPort, NetUtil.getLocalIpStr(), ex);
                throw ex;
            }
        } catch (Exception ex) {
            log.error("request client unknown exception. count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]",
                count, hostId, hostIp, hostPort, NetUtil.getLocalIpStr(), ex);

            Throwable throwable = ex;
            if (ex.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) ex;
                throwable = re.getLastFailedAttempt().getExceptionCause();
                if (throwable instanceof ResourceAccessException) {
                    // 若重试之后该接口仍然有问题，进行路由剔除处理
                    CacheRegisterTable.remove(groupName, namespaceId, hostId);
                }
            }

            throw throwable;
        }

        return null;
    }

    private Retryer<Result> buildResultRetryer() {
        Retryer<Result> retryer = RetryerBuilder.<Result>newBuilder()
            .retryIfException(throwable -> failRetry)
            .withStopStrategy(StopStrategies.stopAfterAttempt(retryTimes <= 0 ? 1 : retryTimes))
            .withWaitStrategy(WaitStrategies.fixedWait(Math.max(retryInterval, 0), TimeUnit.SECONDS))
            .withRetryListener(retryListener)
            .build();
        return retryer;
    }

    private StringBuilder getUrl(Mapping mapping, Map<String, Object> paramMap) {
        StringBuilder url = new StringBuilder();

        String format = NetUtil.getUrl(hostIp, hostPort, contextPath).replaceAll("/+$", StrUtil.EMPTY);
        url.append(format).append(StrUtil.SLASH).append(mapping.path().replaceFirst("^/+", StrUtil.EMPTY));
        if (!CollectionUtils.isEmpty(paramMap)) {
            String query = URLUtil.buildQuery(paramMap, null);
            url.append("?").append(query);
        }

        return url;
    }

    private ParseParasResult doParseParams(Method method, Object[] args) {

        Object body = null;
        HttpHeaders requestHeaders = new HttpHeaders();
        Map<String, Object> paramMap = new HashMap<>();
        // 解析参数
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Body.class)) {
                body = args[i];
            } else if ((parameter.isAnnotationPresent(Header.class))) {
                requestHeaders.add(SystemConstants.EASY_RETRY_HEAD_KEY, JsonUtil.toJsonString(args[i]));
            } else if ((parameter.isAnnotationPresent(Param.class))) {
                paramMap.put(parameter.getAnnotation(Param.class).name(), args[i]);
            } else {
                throw new EasyRetryServerException("parameter error");
            }
        }

        ParseParasResult parseParasResult = new ParseParasResult();
        parseParasResult.setBody(body);
        parseParasResult.setParamMap(paramMap);
        parseParasResult.setRequestHeaders(requestHeaders);
        return parseParasResult;
    }

    @Data
    private static class ParseParasResult {

        private Object body = null;
        private HttpHeaders requestHeaders;
        private Map<String, Object> paramMap;
    }
}
