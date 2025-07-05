package com.aizuda.snailjob.server.mapper;

import cn.hutool.core.util.RandomUtil;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试 message 字段超4K长度的CLOB插入
 */
@SpringBootTest
public class JobLogMessageMapperTest {

    @Autowired
    private JobLogMessageMapper jobLogMessageMapper;

    @Test
    public void test_insertBatch() {
        List<JobLogMessage> jobLogMessages = new ArrayList<>();

        JobLogMessage jobLogMessage1 = new JobLogMessage();
        jobLogMessage1.setJobId(1L);
        jobLogMessage1.setNamespaceId("dev");
        jobLogMessage1.setTaskId(1L);
        jobLogMessage1.setGroupName("ruoyi_group");
        jobLogMessage1.setTaskBatchId(1L);
        jobLogMessage1.setRealTime(1725323299365L);
        jobLogMessage1.setMessage(generateMessage());
        jobLogMessage1.setLogNum(1);
        jobLogMessages.add(jobLogMessage1);

        JobLogMessage jobLogMessage2 = new JobLogMessage();
        jobLogMessage2.setJobId(2L);
        jobLogMessage2.setNamespaceId("dev");
        jobLogMessage2.setTaskId(2L);
        jobLogMessage2.setGroupName("ruoyi_group");
        jobLogMessage2.setTaskBatchId(1L);
        jobLogMessage2.setRealTime(1725323299365L);
        jobLogMessage2.setMessage(generateMessage());
        jobLogMessage2.setLogNum(2);
        jobLogMessages.add(jobLogMessage2);

        jobLogMessageMapper.insertBatch(jobLogMessages);
    }

    String generateMessage() {
        return RandomUtil.randomString(5_000_0000);
    }
}