package com.example;

import com.aizuda.easy.retry.common.core.model.Result;
import com.example.demo.NestMethodService;
import com.example.demo.RemoteService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-27 22:40
 */
@SpringBootTest
public class NestMethodServiceTest {

    @Autowired
    private NestMethodService nestMethodService;
    @MockBean
    private RemoteService remoteService;

    @SneakyThrows
    @Test
    public void testNestMethod() {
        Mockito.when(remoteService.call())
                .thenReturn(new Result(0, "1"))
                .thenReturn(new Result(0, "2"))
                .thenReturn(new Result(0, "3"))
                .thenReturn(new Result(0, "4"))
                .thenReturn(new Result(1, "5"))
        ;
        try {
            nestMethodService.testNestMethod();
        } catch (Exception e) {
            System.out.println(e);
        }

        Thread.sleep(90000);
    }
}
