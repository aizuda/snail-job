package com.example.demo;

import com.aizuda.easy.retry.client.core.annotation.Retryable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
}
