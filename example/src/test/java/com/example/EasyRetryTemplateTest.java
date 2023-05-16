package com.example;

import com.aizuda.easy.retry.client.core.retryer.EasyRetryTemplate;
import com.aizuda.easy.retry.client.core.retryer.RetryTaskTemplateBuilder;
import com.aizuda.easy.retry.common.core.model.Result;
import com.example.demo.CustomAsyncCreateTask;
import com.example.demo.CustomSyncCreateTask;
import com.example.demo.NestMethodService;
import com.example.demo.RemoteService;
import com.example.model.Cat;
import com.example.model.Zoo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author: www.byteblogs.com
 * @date : 2023-05-10 13:47
 */
@SpringBootTest
@Slf4j
public class EasyRetryTemplateTest {

    @Autowired
    private NestMethodService nestMethodService;
    @MockBean
    private RemoteService remoteService;

    @Test
    public void generateAsyncTaskTest() throws InterruptedException {

        Cat cat = new Cat();
        cat.setName("zsd");
        Zoo zoo = new Zoo();
        zoo.setNow(LocalDateTime.now());
        EasyRetryTemplate retryTemplate = RetryTaskTemplateBuilder.newBuilder()
            .withExecutorMethod(CustomAsyncCreateTask.class)
            .withParam(zoo)
            .withScene(CustomAsyncCreateTask.SCENE)
            .build();

        retryTemplate.executeRetry();

        Thread.sleep(90000);
    }

    @Test
    public void generateSyncTask() {

        Zoo zoo = new Zoo();
        zoo.setNow(LocalDateTime.now());
        EasyRetryTemplate retryTemplate = RetryTaskTemplateBuilder.newBuilder()
            .withExecutorMethod(CustomSyncCreateTask.class)
            .withParam(zoo)
            .withScene(CustomSyncCreateTask.SCENE)
            .build();

        retryTemplate.executeRetry();

    }

    @Test
    public void testNestMethodForCustomSyncCreateTask() throws InterruptedException {
        nestMethodService.testNestMethodForCustomSyncCreateTask();

        Thread.sleep(90000);
    }

}
