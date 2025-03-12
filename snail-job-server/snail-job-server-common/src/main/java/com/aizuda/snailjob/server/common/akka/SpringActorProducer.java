package com.aizuda.snailjob.server.common.akka;

import org.apache.pekko.actor.Actor;
import org.apache.pekko.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

/**
 * @author opensnail
 * @date 2021-02-10
 */
public class SpringActorProducer implements IndirectActorProducer {

    private final ApplicationContext applicationContext;
    private final String actorBeanName;
    private final Object[] args;

    public SpringActorProducer(ApplicationContext applicationContext, String actorBeanName, Object... args) {
        this.applicationContext = applicationContext;
        this.actorBeanName = actorBeanName;
        this.args = args;
    }

    public SpringActorProducer(ApplicationContext applicationContext, String actorBeanName) {
        this.applicationContext = applicationContext;
        this.actorBeanName = actorBeanName;
        this.args = null;
    }

    @Override
    public Actor produce() {
        if (args == null) {
            return (Actor) applicationContext.getBean(actorBeanName);
        } else {
            return (Actor) applicationContext.getBean(actorBeanName, args);
        }
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
    }

}
