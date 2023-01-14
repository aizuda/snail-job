package com.example;

import com.example.demo.TestRetryStrategyService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:03
 */
@SpringBootTest
public class TestRetryStrategyServiceTest {

    @Autowired
    private TestRetryStrategyService testRetryStrategyService;

    @Test
    public void errorMethodForOnlyLocal() {
        testRetryStrategyService.errorMethodForOnlyLocal(UUID.randomUUID().toString());
    }

    @SneakyThrows
    @Test
    public void errorMethodForOnlyRemote() {
        testRetryStrategyService.errorMethodForOnlyRemote(UUID.randomUUID().toString());
        Thread.sleep(90000);
    }

}
