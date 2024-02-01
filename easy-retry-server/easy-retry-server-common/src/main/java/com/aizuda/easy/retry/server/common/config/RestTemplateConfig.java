package com.aizuda.easy.retry.server.common.config;

import com.aizuda.easy.retry.server.common.client.RequestInterceptor;
import com.aizuda.easy.retry.server.common.okhttp.OkHttp3ClientHttpRequestFactory;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-09 14:19
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean
    public ClientHttpRequestFactory okHttp3ClientHttpRequestFactory() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            // 整个流程耗费的超时时间
            .callTimeout(60, TimeUnit.SECONDS)
            // 读取耗时
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            // 三次握手 + SSL建立耗时
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            // 写入耗时
            .writeTimeout(5000, TimeUnit.MILLISECONDS)
            // 当连接失败，尝试重连
            .retryOnConnectionFailure(true)
            // 最大空闲连接数及连接的保活时间进行配置
            .connectionPool(new ConnectionPool(200, 5, TimeUnit.MINUTES))
            .addInterceptor(new RequestInterceptor())
            .build();

        return new OkHttp3ClientHttpRequestFactory(okHttpClient);
    }

}
