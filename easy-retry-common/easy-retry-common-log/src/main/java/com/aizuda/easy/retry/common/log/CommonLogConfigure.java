package com.aizuda.easy.retry.common.log;

import com.aizuda.easy.retry.common.log.factory.GlobalLogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author: xiaowoniu
 * @date : 2024-01-16 10:52
 * @since : 2.6.0
 */
@Configuration
@ComponentScan("com.aizuda.easy.retry.common.log.*")
public class CommonLogConfigure {

    @Autowired
    public void setEnvironment(Environment environment) {
        GlobalLogFactory.setEnvironment(environment);
    }

}
