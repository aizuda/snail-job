package com.example.controller;


import com.x.retry.client.core.annotation.Retryable;
import com.x.retry.common.core.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * 教师 前端控制器
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2022-03-24
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("test-ddl")
    public Result testDDL() {
        Result result = restTemplate.getForObject("http://127.0.0.1:8088/school/id", Result.class);
        result = restTemplate.getForObject("http://127.0.0.1:8088/school/id", Result.class);
        result = restTemplate.getForObject("http://127.0.0.1:8088/school/id", Result.class);
        result = restTemplate.getForObject("http://127.0.0.1:8088/school/id", Result.class);

        if (result.getStatus() == 0) {
            throw new UnsupportedOperationException(result.getMessage());
        }
        return result;
    }

    @GetMapping("test-status-code")
    @Retryable(scene = "testStatusCode")
    public Result testStatusCode() {
        Result result = restTemplate.getForObject("http://127.0.0.1:8088/school/id", Result.class);

        if (result.getStatus() == 0) {
            throw new UnsupportedOperationException(result.getMessage());
        }
        return result;
    }
}
