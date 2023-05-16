package com.example.demo;

import com.aizuda.easy.retry.client.core.annotation.Retryable;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot;
import com.aizuda.easy.retry.client.core.intercepter.RetrySiteSnapshot.EnumStage;
import com.aizuda.easy.retry.client.core.retryer.EasyRetryTemplate;
import com.aizuda.easy.retry.client.core.retryer.RetryTaskTemplateBuilder;
import com.aizuda.easy.retry.common.core.model.Result;
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
    @Autowired
    private RemoteService remoteService;

    @Retryable(scene = "testNestMethod" , isThrowException = false)
    @Transactional
    public void testNestMethod() {
        testExistsTransactionalRetryService.testSimpleInsert(UUID.randomUUID().toString());
    }

    @Retryable(scene = "testNestMethodForCustomSyncCreateTask" , isThrowException = false)
    @Transactional
    public void testNestMethodForCustomSyncCreateTask() {

        if (RetrySiteSnapshot.getStage() == null || RetrySiteSnapshot.getStage() == EnumStage.LOCAL.getStage()) {
            throw new EasyRetryClientException("测试注解重试和手动重试");
        }

        // 同步强制上报
        Zoo zoo = new Zoo();
        zoo.setNow(LocalDateTime.now());
        EasyRetryTemplate retryTemplate = RetryTaskTemplateBuilder.newBuilder()
            .withExecutorMethod(CustomSyncCreateTask.class)
            .withParam(zoo)
            .withScene(CustomSyncCreateTask.SCENE)
            .build();

        retryTemplate.executeRetry();

        // 异步强制上报
        zoo.setNow(LocalDateTime.now());
        retryTemplate = RetryTaskTemplateBuilder.newBuilder()
            .withExecutorMethod(CustomAsyncCreateTask.class)
            .withParam(zoo)
            .withScene(CustomAsyncCreateTask.SCENE)
            .build();
        retryTemplate.executeRetry();
    }
}
