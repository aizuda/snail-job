package com.aizuda.snailjob.common.core;

import com.aizuda.snailjob.common.core.network.SnailJobNetworkProperties;
import com.aizuda.snailjob.common.core.util.SnailJobNetworkUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: opensnail
 * @date : 2021-11-25 10:52
 */
@Configuration
@ComponentScan("com.aizuda.snailjob.common.core.*")
public class CommonCoreConfigure {

    @Bean
    public SnailJobNetworkUtils snailJobNetworkUtils(SnailJobNetworkProperties snailJobNetworkProperties) {
        return new SnailJobNetworkUtils(snailJobNetworkProperties);
    }

}
