package com.aizuda.snailjob.server.mapper;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试 message 字段超4K长度的CLOB插入
 */
@SpringBootTest
public class RetryLogMessageMapperTest {

    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;

    @Test
    public void test_insertBatch() {
        List<RetryTaskLogMessage> retryTaskLogMessages = new ArrayList<>();

        RetryTaskLogMessage retryTaskLogMessage1 = new RetryTaskLogMessage();
        retryTaskLogMessage1.setNamespaceId("dev");
        retryTaskLogMessage1.setGroupName("ruoyi_group");
        retryTaskLogMessage1.setRealTime(1725323299365L);
        retryTaskLogMessage1.setMessage(generateMessage());
        retryTaskLogMessage1.setLogNum(1);
        retryTaskLogMessages.add(retryTaskLogMessage1);

        RetryTaskLogMessage retryTaskLogMessage2 = new RetryTaskLogMessage();
        retryTaskLogMessage2.setNamespaceId("dev");
        retryTaskLogMessage2.setGroupName("ruoyi_group");
        retryTaskLogMessage2.setRealTime(1725323299365L);
        retryTaskLogMessage2.setMessage(generateMessage());
        retryTaskLogMessage2.setLogNum(1);
        retryTaskLogMessages.add(retryTaskLogMessage2);

        retryTaskLogMessageMapper.insertBatch(retryTaskLogMessages);
    }

    String generateMessage() {
        return RandomUtil.randomString(5_000_0000);
    }
}