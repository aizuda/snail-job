package com.example;

import com.example.demo.RemoteService;
import com.example.demo.TestExistsTransactionalRetryService;
import com.x.retry.common.core.model.Result;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static org.awaitility.Awaitility.await;

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

//        await().atLeast(1, TimeUnit.MINUTES);
        Thread.sleep(90000);

    }
}
