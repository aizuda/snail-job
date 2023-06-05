package com.aizuda.easy.retry.server.server.handler;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.service.RetryService;
import com.aizuda.easy.retry.server.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.aizuda.easy.retry.common.core.constant.SystemConstants.HTTP_PATH.BATCH_REPORT;

/**
 * 处理上报数据
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-07 16:39
 * @since 1.0.0
 */
@Component
@Slf4j
public class ReportRetryInfoHttpRequestHandler extends PostHttpRequestHandler {

    @Autowired
    private RetryService retryService;

    @Override
    public boolean supports(String path) {
        return BATCH_REPORT.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public String doHandler(String content, UrlQuery urlQuery, HttpHeaders  headers) {
        LogUtils.info(log, "批量上报重试数据 content:[{}]", content);

        EasyRetryRequest retryRequest = JsonUtil.parseObject(content, EasyRetryRequest.class);

        try {
            Object[] args = retryRequest.getArgs();

            Boolean aBoolean = retryService.batchReportRetry(JsonUtil.parseList(JsonUtil.toJsonString(args[0]), RetryTaskDTO.class));
            return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), "批量上报重试数据处理成功", aBoolean, retryRequest.getReqId()));
        } catch (Exception e) {
            LogUtils.error(log, "批量上报重试数据失败", e);
            return JsonUtil.toJsonString(new NettyResult(StatusEnum.YES.getStatus(), e.getMessage(), null, retryRequest.getReqId()));
        }
    }
}
