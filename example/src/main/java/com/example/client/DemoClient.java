package com.example.client;

//import org.springframework.cloud.netflix.feign.FeignClient;
import com.x.retry.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-16 15:32
 */
@FeignClient(name = "daemoClient", url = "http://127.0.0.1:8089")
public interface DemoClient {

    @GetMapping("/school/id")
    Result get();
}
