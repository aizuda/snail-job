package com.example;

import com.example.demo.RetryRegisterService;
import com.example.model.Cat;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 17:29
 */
@SpringBootTest
public class RetryRegisterServiceTest {

    @Autowired
    private RetryRegisterService retryRegisterService;

    @SneakyThrows
    @Test
    public void errorMethod1() {

        try {
            List<Cat> list = new ArrayList<>();
            Cat cat = new Cat();
            cat.setName(UUID.randomUUID().toString());
            list.add(cat);
            retryRegisterService.errorMethod1(list);
        } catch (Exception e) {
        }

         Thread.sleep(90000);

    }

}
