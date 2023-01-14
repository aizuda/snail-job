package com.x.retry.server.mapper;

import com.x.retry.server.config.RequestDataHelper;
import com.x.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-03 18:03
 */
@SpringBootTest
public class RetryTaskMapperTest {

    @Autowired
    private RetryTaskMapper retryTaskMapper;

    @Test
    public void test() {
        RequestDataHelper.setPartition(0);
        RetryTask retryTask = retryTaskMapper.selectById(1);
        System.out.println(retryTask);
    }
}
