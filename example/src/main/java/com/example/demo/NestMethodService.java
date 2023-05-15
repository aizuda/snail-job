package com.example.demo;

import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.core.retryer.EasyRetryTemplate;
import com.aizuda.easy.retry.client.core.retryer.RetryTaskTemplateBuilder;
import com.example.model.Zoo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-27 22:37
 */
@Component
public class NestMethodService {

    @Autowired
    private TestExistsTransactionalRetryService testExistsTransactionalRetryService;

    @Retryable(scene = "testNestMethod" , isThrowException = false)
    @Transactional
    public void testNestMethod() {
        testExistsTransactionalRetryService.testSimpleInsert(UUID.randomUUID().toString());
    }

    @Retryable(scene = "testNestMethodForCustomSyncCreateTask" , isThrowException = false)
    @Transactional
    public void testNestMethodForCustomSyncCreateTask() {

        Random random = new Random();
        int i = random.nextInt(5);
        if (i <= 2) {
            throw new EasyRetryClientException("测试注解重试和手动重试");
        }

        Zoo zoo = new Zoo();
        zoo.setNow(LocalDateTime.now());
        EasyRetryTemplate retryTemplate = RetryTaskTemplateBuilder.newBuilder()
            .withExecutorMethod(CustomSyncCreateTask.class)
            .withParam(zoo)
            .withScene(CustomSyncCreateTask.SCENE)
            .build();

        retryTemplate.executeRetry();
    }
}
