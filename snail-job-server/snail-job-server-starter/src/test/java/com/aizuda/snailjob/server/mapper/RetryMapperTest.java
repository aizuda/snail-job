package com.aizuda.snailjob.server.mapper;

import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.utils.RequestDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author: opensnail
 * @date : 2021-11-03 18:03
 */
@SpringBootTest
public class RetryMapperTest {

    @Autowired
    private RetryMapper retryMapper;

    @Test
    public void test() {
        RequestDataHelper.setPartition(0);
        Retry retry = retryMapper.selectById(1);
        System.out.println(retry);
    }
}
