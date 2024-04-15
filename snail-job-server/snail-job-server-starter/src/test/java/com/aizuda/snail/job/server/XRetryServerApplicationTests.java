package com.aizuda.snail.job.server;

import com.google.common.util.concurrent.RateLimiter;
import lombok.SneakyThrows;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@SpringBootTest
class XRetryServerApplicationTests {

    @SneakyThrows
    public static void main(String[] args) {
        RateLimiter rateLimiter = RateLimiter.create(20);
        for (int i = 0; i < 10000; i++) {
            int finalI = i;
            if (i % 100 == 0) {
                Thread.sleep(1000);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {

                    if (!rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
                        System.out.println("短期无法获取令牌，真不幸，排队也瞎排  " + finalI);
                    } else {
                        System.out.println(new Date() + " " + finalI);
                    }
                }
            }).start();
        }

        Thread.sleep(90000L);
    }


}
