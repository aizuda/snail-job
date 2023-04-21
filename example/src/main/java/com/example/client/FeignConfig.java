package com.example.client;

import com.aizuda.easy.retry.client.core.plugin.ResponseHeaderPlugins;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.StringDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-17 08:03
 */
@Configuration
public class FeignConfig {

    @Bean
    public Decoder decoder(@Autowired HttpMessageConverters httpMessageConverters) {

        ObjectFactory<HttpMessageConverters> objectFactory = () -> httpMessageConverters;

        return new SpringDecoder(objectFactory) {
            @Override
            public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {

                Map<String, Collection<String>> headers = response.headers();

                Map<String, List<String>> header = new HashMap<>();
                headers.forEach((key, value)-> {
                    header.put(key, (List<String>) value);
                });

                ResponseHeaderPlugins.responseHeader(header);
                return super.decode(response, type);
            }
        };
    }
}
