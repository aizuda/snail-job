package com.aizuda.snailjob.server.support.generator.id;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.generator.id.IdGenerator;
import com.aizuda.snailjob.server.common.generator.id.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 测试多线程情况下号段模式的运行情况
 *
 * @author: opensnail
 * @date : 2023-05-06 09:16
 * @since 1.2.0
 */
@SpringBootTest
@Slf4j
public class SegmentIdGeneratorTest {

    @Autowired
    @Qualifier("segmentIdGenerator")
    private IdGenerator idGenerator;

    @Test
    public void idGeneratorTest() throws InterruptedException {

        // step: 100, cpu: 4 memory: 16G, 线程1-150之间出现异常id:-3的情况较少。
        // 如果有特殊需求可以提高step， 可通过系统配置: SystemProperties#step属性进行配置
        Set<String> idSet = new HashSet<>();
        int size = 500;
        CountDownLatch count = new CountDownLatch(size);
        for (int i = 0; i < size; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    count.countDown();

                    String id = idGenerator.idGenerator("example_group", SystemConstants.DEFAULT_NAMESPACE);
                    SnailJobLog.LOCAL.info("id:[{}]", id);
                    if (Long.parseLong(id) < 0) {
                        throw new SnailJobServerException("exception id");
                    } else if (idSet.contains(id)) {
                        throw new SnailJobServerException("duplicate id [{}]", id);
                    } else {
                        idSet.add(id);
                    }
                }
            }).start();
        }

        count.await();
    }
}
