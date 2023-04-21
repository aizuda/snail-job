package com.example.demo;

import com.aizuda.easy.retry.common.core.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-26 10:14
 */
@Component
public class RemoteService {

    @Autowired
    private RestTemplate restTemplate;

    public Result call() {
       return restTemplate.getForObject("http://127.0.0.1:8088/school/id", Result.class);
//        return new Result();
    }
}
