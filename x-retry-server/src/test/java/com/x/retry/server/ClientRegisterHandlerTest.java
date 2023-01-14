package com.x.retry.server;

import com.x.retry.server.support.handler.ClientRegisterHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-02 22:38
 */
@SpringBootTest
public class ClientRegisterHandlerTest {

    @Autowired
    private ClientRegisterHandler clientRegisterHandler;

    @SneakyThrows
    @Test
    public void syncVersion() {

        clientRegisterHandler.syncVersion(null, "example_group", "127.0.0.1", 8089);
        Thread.sleep(10000L);

    }

}
