package com.aizuda.easy.retry.server.common.akka;

import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建ActorSystem，并将其放入到spring管理，初始化ApplicationContext
 *
 * @author www.byteblogs.com
 * @date 2021-11-20
 */
@Configuration
public class AkkaConfiguration {

    private static final String DISPATCH_RETRY_ACTOR_SYSTEM = "DISPATCH_RETRY_ACTOR_SYSTEM";
    private static final String DISPATCH_EXEC_UNIT_RETRY_ACTOR_SYSTEM = "DISPATCH_EXEC_UNIT_RETRY_ACTOR_SYSTEM";
    private static final String DISPATCH_RESULT_ACTOR_SYSTEM = "DISPATCH_RESULT_ACTOR_SYSTEM";
    private static final String LOG_ACTOR_SYSTEM = "LOG_ACTOR_SYSTEM";
    private static final String NETTY_ACTOR_SYSTEM = "NETTY_ACTOR_SYSTEM";

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SpringExtension springExtension;

    /**
     * 重试分发ActorSystem
     *
     * @return {@link ActorSystem} 顶级actor
     */
    @Bean("dispatchRetryActorSystem")
    public ActorSystem createDispatchRetryActorSystem() {
        ActorSystem system = ActorSystem.create(DISPATCH_RETRY_ACTOR_SYSTEM);
        springExtension.initialize(applicationContext);
        return system;
    }

    /**
     * 重试分发执行ActorSystem
     *
     * @return {@link ActorSystem} 顶级actor
     */
    @Bean("dispatchExecUnitActorSystem")
    public ActorSystem createDispatchExecUnitRetryActorSystem() {
        ActorSystem system = ActorSystem.create(DISPATCH_EXEC_UNIT_RETRY_ACTOR_SYSTEM);
        springExtension.initialize(applicationContext);
        return system;
    }


    /**
     * 重试分发执行结果处理ActorSystem
     *
     * @return {@link ActorSystem} 顶级actor
     */
    @Bean("dispatchResultActorSystem")
    public ActorSystem createDispatchResultRetryActorSystem() {
        ActorSystem system = ActorSystem.create(DISPATCH_RESULT_ACTOR_SYSTEM);
        springExtension.initialize(applicationContext);
        return system;
    }

    /**
     * 日志处理
     *
     * @return {@link ActorSystem} 顶级actor
     */
    @Bean("logActorSystem")
    public ActorSystem createLogActorSystem() {
        ActorSystem system = ActorSystem.create(LOG_ACTOR_SYSTEM);
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
        ActorSystem system = ActorSystem.create(NETTY_ACTOR_SYSTEM);
        springExtension.initialize(applicationContext);
        return system;
    }
}
