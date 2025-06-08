package com.aizuda.snailjob.server.common.config;

import com.aizuda.snailjob.server.common.Register;
import com.aizuda.snailjob.server.common.Schedule;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.register.ClientRegister;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * web访问模块
 *
 * @author: opensnail
 * @date : 2023-09-19 09:21
 * @since : 2.4.0
 */
@Configuration
@ComponentScan("com.aizuda.snailjob.server.common.*")
public class SnailJobServerCommonAutoConfiguration {

    @Bean
    public TaskScheduler scheduledExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("snail-job-scheduled-thread-");
        return scheduler;
    }

    @Bean
    public TaskScheduler alarmExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("snail-job-alarm-thread-");
        return scheduler;
    }

    @DependsOn(value = ClientRegister.BEAN_NAME)
    @Bean
    public Schedule refreshNodeSchedule(ClientRegister clientRegister, ServerNodeMapper serverNodeMapper,
                                        InstanceManager instanceManager) {
        return clientRegister.newRefreshNodeSchedule(serverNodeMapper, instanceManager);
    }

    @Bean(ClientRegister.BEAN_NAME)
    public Register clientRegister() {
        return new ClientRegister();
    }


}
