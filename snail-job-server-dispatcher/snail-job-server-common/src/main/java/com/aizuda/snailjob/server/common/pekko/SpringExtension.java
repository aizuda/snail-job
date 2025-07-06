package com.aizuda.snailjob.server.common.pekko;

import org.apache.pekko.actor.Extension;
import org.apache.pekko.actor.Props;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 扩展组件，ApplicationContext会在SpringBoot初始化的时候加载进来
 * 构造Props,用于生产ActorRef
 *
 * @author opensnail
 * @date 2021-02-10
 */
@Component
public class SpringExtension implements Extension {

    private ApplicationContext applicationContext;

    public void initialize(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Props props(String actorBeanName) {
        return Props.create(SpringActorProducer.class, applicationContext, actorBeanName);
    }

    public Props props(String actorBeanName, Object... args) {
        return Props.create(SpringActorProducer.class, applicationContext, actorBeanName, args);
    }

}
