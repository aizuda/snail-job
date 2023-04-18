package com.aizuda.easy.retry.server.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.service.RetryService;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.model.XRetryRequest;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:39
 */
@Component
@Slf4j
public class ReportRetryInfoHttpRequestHandler extends PostHttpRequestHandler {

    private static final String URI = "/batch/report";

    @Autowired
    private RetryService retryService;

    @Override
    public boolean supports(String uri) {
        return URI.equals(uri);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public String doHandler(String content, UrlQuery urlQuery, HttpHeaders  headers) {
        LogUtils.info(log, "批量上报重试数据 content:[{}]", content);

        XRetryRequest retryRequest = JsonUtil.parseObject(content, XRetryRequest.class);

        try {
            Boolean aBoolean = retryService.batchReportRetry(JsonUtil.parseList(JsonUtil.toJsonString(retryRequest.getArgs()), RetryTaskDTO.class));
            return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), "批量上报重试数据处理成功", aBoolean, retryRequest.getRequestId()));
        } catch (Exception e) {
            LogUtils.error(log, "批量上报重试数据失败", e);
            return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), e.getMessage(), null, retryRequest.getRequestId()));
        }
    }
}
