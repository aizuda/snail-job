package com.example;

import com.x.retry.client.core.plugin.RequestHeaderPlugins;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-16 15:05
 */
@Component
public class ExampleFeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {

        Map<String, String> header = RequestHeaderPlugins.requestHeader();
        header.forEach((key, value) -> requestTemplate.header(key, value));

    }

}
