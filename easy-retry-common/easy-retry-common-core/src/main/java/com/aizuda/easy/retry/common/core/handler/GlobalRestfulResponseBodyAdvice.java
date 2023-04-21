package com.aizuda.easy.retry.common.core.handler;

import com.aizuda.easy.retry.common.core.annotation.OriginalControllerReturnValue;
import com.aizuda.easy.retry.common.core.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.Annotation;

@Slf4j
@ControllerAdvice(basePackages = {"com.aizuda.easy.retry.client.core", "com.aizuda.easy.retry.server"})
public class GlobalRestfulResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public Object beforeBodyWrite(
            Object obj, MethodParameter methodParameter, MediaType mediaType,
            Class<? extends HttpMessageConverter<?>> converterType,
            ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        Annotation originalControllerReturnValue = methodParameter.getMethodAnnotation(OriginalControllerReturnValue.class);
        if (originalControllerReturnValue != null) {
            return obj;
        }

        if (obj instanceof Result) {
           return obj;
        } else {
            return new Result<>("请求成功", obj);
        }

    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

}
