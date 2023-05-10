package com.example;

import com.example.demo.TestRetryMethodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 14:09
 */
@SpringBootTest
public class TestRetryMethodServiceTest {

    @Autowired
    public TestRetryMethodService retryMethodService;

    @Test
    public void testRetryMethod() throws InterruptedException {
        try {
            retryMethodService.testRetryMethod(UUID.randomUUID().toString());
        }catch (Exception e) {

        }
        Thread.sleep(90000);
    }

}
