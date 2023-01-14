package com.example;

import com.example.demo.RemoteService;
import com.example.demo.TestExistsTransactionalRetryService;
import com.example.demo.TestExistsTransactionalRetryService2;
import com.x.retry.common.core.model.Result;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-26 09:12
 */
@SpringBootTest
@Slf4j
public class ExistsTransactionalRetryServiceTest {

    @Autowired
    private TestExistsTransactionalRetryService testExistsTransactionalRetryService;
    @MockBean
    private RemoteService remoteService;
    @Autowired
    private TestExistsTransactionalRetryService2 testExistsTransactionalRetryService2;

    @SneakyThrows
    @Test
    public void testSimpleInsert() {

        Mockito.when(remoteService.call())
                .thenReturn(new Result(0, "1"))
                .thenReturn(new Result(0, "2"))
                .thenReturn(new Result(0, "3"))
                .thenReturn(new Result(0, "4"))
                .thenReturn(new Result(0, "5"))
        ;
        try {
            String s = testExistsTransactionalRetryService.testSimpleInsert(UUID.randomUUID().toString());
            System.out.println(s);
        } catch (Exception e) {
            log.error("", e);
        }

        Thread.sleep(90000);

    }

    @SneakyThrows
    @Test
    public void testSimpleUpdate() {

        Mockito.when(remoteService.call())
                .thenReturn(new Result(0, "1"))
                .thenReturn(new Result(0, "2"))
                .thenReturn(new Result(0, "3"))
                .thenReturn(new Result(0, "4"))
                .thenReturn(new Result(0, "5"))
        ;
        try {
            String s = testExistsTransactionalRetryService2.testSimpleUpdate(243L);
            System.out.println(s);
        } catch (Exception e) {
            log.error("", e);
        }

        Thread.sleep(90000);

    }

    @SneakyThrows
    @Test
    public void syncTestSimpleInsert() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 50, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        Mockito.when(remoteService.call())
                .thenReturn(new Result(0, "1"))
                .thenReturn(new Result(0, "2"))
                .thenReturn(new Result(0, "3"))
                .thenReturn(new Result(0, "4"))
                .thenReturn(new Result(0, "5"))
        ;
        try {
            for (int i = 0; i < 400; i++) {
                threadPoolExecutor.execute(() -> testExistsTransactionalRetryService.testSimpleInsert(UUID.randomUUID().toString()));
            }
        } catch (Exception e) {
            log.error("", e);
        }

        Thread.sleep(900000);

    }
}
