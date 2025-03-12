package com.aizuda.snailjob.server.common.akka;

import org.apache.pekko.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建ActorSystem，并将其放入到spring管理，初始化ApplicationContext
 *
 * @author opensnail
 * @date 2021-11-20
 */
@Configuration
public class AkkaConfiguration {

    private static final String CONFIG_NAME = "snailjob";
    private static final String NETTY_ACTOR_SYSTEM = "NETTY_ACTOR_SYSTEM";
    private static final String JOB_ACTOR_SYSTEM = "JOB_ACTOR_SYSTEM";
    private static final String RETRY_ACTOR_SYSTEM = "RETRY_ACTOR_SYSTEM";
    private static final String COMMON_ACTOR_SYSTEM = "COMMON_ACTOR_SYSTEM";


    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SpringExtension springExtension;

    /**
     * 日志处理
     *
     * @return {@link ActorSystem} 顶级actor
     */
    @Bean("commonActorSystem")
    public ActorSystem createLogActorSystem() {
        ActorSystem system = ActorSystem.create(COMMON_ACTOR_SYSTEM, ConfigFactory.load(CONFIG_NAME));
        springExtension.initialize(applicationContext);
        return system;
    }

    /**
     * 处理netty客户端请求
     *
     * @return {@link ActorSystem} 顶级actor
     */
    @Bean("nettyActorSystem")
    public ActorSystem nettyActorSystem() {
        ActorSystem system = ActorSystem.create(NETTY_ACTOR_SYSTEM, ConfigFactory.load(CONFIG_NAME));
        springExtension.initialize(applicationContext);
        return system;
    }

    /**
     * 处理retry调度
     *
     * @return {@link ActorSystem} 顶级actor
     */
    @Bean("retryActorSystem")
    public ActorSystem retryActorSystem() {
        ActorSystem system = ActorSystem.create(RETRY_ACTOR_SYSTEM, ConfigFactory.load(CONFIG_NAME));
        springExtension.initialize(applicationContext);
        return system;
    }


    /**
     * 处理job调度
     *
     * @return {@link ActorSystem} 顶级actor
     */
    @Bean("jobActorSystem")
    public ActorSystem jobActorSystem() {
        ActorSystem system = ActorSystem.create(JOB_ACTOR_SYSTEM, ConfigFactory.load(CONFIG_NAME));
        springExtension.initialize(applicationContext);
        return system;
    }

}
