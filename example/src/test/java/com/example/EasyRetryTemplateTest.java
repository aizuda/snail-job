package com.example;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.core.retryer.EasyRetryTemplate;
import com.aizuda.easy.retry.client.core.retryer.RetryTaskTemplateBuilder;
import com.example.demo.CustomCreateTask;
import com.example.model.Cat;
import com.example.model.Zoo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author: shuguang.zhang
 * @date : 2023-05-10 13:47
 */
@SpringBootTest
@Slf4j
public class EasyRetryTemplateTest {

    @Test
    public void generateAsyncTaskTest() throws InterruptedException {

        Cat cat = new Cat();
        cat.setName("zsd");
        Zoo zoo = new Zoo();
        zoo.setNow(LocalDateTime.now());
        EasyRetryTemplate retryTemplate = RetryTaskTemplateBuilder.newBuilder()
            .withExecutorMethod(CustomCreateTask.class)
            .withParam(zoo)
            .withScene(CustomCreateTask.SCENE)
            .build();

        retryTemplate.generateAsyncTask(true);

        Thread.sleep(90000);
    }

    @Test
    public void generateSyncTask() throws InterruptedException {

        Cat cat = new Cat();
        cat.setName("zsd");
        Zoo zoo = new Zoo();
        zoo.setNow(LocalDateTime.now());
        EasyRetryTemplate retryTemplate = RetryTaskTemplateBuilder.newBuilder()
            .withExecutorMethod(CustomCreateTask.class)
            .withParam(zoo)
            .withScene(CustomCreateTask.SCENE)
            .build();

        Boolean aBoolean = retryTemplate.generateSyncTask(true);
        Assert.isTrue(aBoolean);

    }
}
