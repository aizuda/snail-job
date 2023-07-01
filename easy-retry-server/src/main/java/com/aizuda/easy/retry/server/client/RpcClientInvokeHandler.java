package com.aizuda.easy.retry.server.client;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.URLUtil;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.HostUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.client.annotation.Body;
import com.aizuda.easy.retry.server.client.annotation.Header;
import com.aizuda.easy.retry.server.client.annotation.Mapping;
import com.aizuda.easy.retry.server.client.annotation.Param;
import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.support.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.support.handler.ClientNodeAllocateHandler;
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

/**
 * 请求处理器
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-11 21:45
 * @since 2.0.0
 */
@Slf4j
public class RpcClientInvokeHandler implements InvocationHandler {

    public static final String URL = "http://{0}:{1}/{2}";

    private final String groupName;
    private String hostId;
    private String hostIp;
    private Integer hostPort;
    private String contextPath;

    public RpcClientInvokeHandler(
        final String groupName,
        final String hostId,
        final String hostIp,
        final Integer hostPort,
        final String contextPath) {
        this.groupName = groupName;
        this.hostId = hostId;
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.contextPath = contextPath;
    }

    @Override
    public Result invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Mapping annotation = method.getAnnotation(Mapping.class);
        Assert.notNull(annotation, () -> new EasyRetryServerException("@Mapping cannot be null"));

        Set<RegisterNodeInfo> serverNodeSet = CacheRegisterTable.getServerNodeSet(groupName);
        // 最多调用size次
        int size = serverNodeSet.size();
        for (int count = 1; count <= size; count++) {
            log.info("Start request client. count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count, hostId, hostIp, hostPort, HostUtils.getIp());
            Result result = requestRemote(method, args, annotation, count);
            if (Objects.nonNull(result)) {
                return result;
            }
        }

        throw new EasyRetryServerException("No available nodes.");
    }

    private Result requestRemote(Method method, Object[] args, Mapping mapping, int count) {

        try {

            // 参数解析
            ParseParasResult parasResult = doParseParams(method, args);

            // 若是POST请求，请求体不能是null
            if (RequestMethod.POST.name().equals(mapping.method().name())) {
                Assert.notNull(parasResult.body, () -> new EasyRetryServerException("body cannot be null"));
            }

            RestTemplate restTemplate = SpringContext.CONTEXT.getBean(RestTemplate.class);

            ResponseEntity<Result> response = restTemplate.exchange(
                // 拼接 url?a=1&b=1
                getUrl(mapping, parasResult.paramMap).toString(),
                // post or get
                HttpMethod.valueOf(mapping.method().name()),
                // body
                new HttpEntity<>(parasResult.body, parasResult.requestHeaders),
                // 返回值类型
                Result.class);

            log.info("Request client success. count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count, hostId, hostIp, hostPort, HostUtils.getIp());

            return Objects.requireNonNull(response.getBody());
        } catch (RestClientException ex) {
            // 网络异常
            if (ex instanceof ResourceAccessException) {
                log.error("request client I/O error, count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count, hostId, hostIp, hostPort, HostUtils.getIp(), ex);

                // 进行路由剔除处理
                CacheRegisterTable.remove(groupName, hostId);
                // 重新选一个可用的客户端节点
                ClientNodeAllocateHandler clientNodeAllocateHandler = SpringContext.CONTEXT.getBean(
                    ClientNodeAllocateHandler.class);
                RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(groupName);
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
                log.error("request client error.count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count, hostId, hostIp, hostPort, HostUtils.getIp(), ex);
                throw ex;
            }
        } catch (Exception ex) {
            log.error("request client unknown exception. count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count, hostId, hostIp, hostPort, HostUtils.getIp(), ex);
            throw ex;
        }

       return null;
    }

    @NotNull
    private StringBuilder getUrl(Mapping mapping, Map<String, Object> paramMap) {
        StringBuilder url = new StringBuilder(MessageFormat.format(URL, hostIp, hostPort.toString(), contextPath));
        url.append(mapping.path());
        if (!CollectionUtils.isEmpty(paramMap)) {
            String query = URLUtil.buildQuery(paramMap, null);
            url.append("?").append(query);
        }

        return url;
    }

    @NotNull
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
