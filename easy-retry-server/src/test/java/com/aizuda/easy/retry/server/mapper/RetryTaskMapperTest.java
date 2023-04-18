package com.aizuda.easy.retry.server.mapper;

import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.config.RequestDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
