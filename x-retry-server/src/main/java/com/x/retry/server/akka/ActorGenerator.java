package com.x.retry.server.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.x.retry.common.core.context.SpringContext;
import com.x.retry.server.support.dispatch.actor.exec.ExecUnitActor;
import com.x.retry.server.support.dispatch.actor.result.FailureActor;
import com.x.retry.server.support.dispatch.actor.result.FinishActor;
import com.x.retry.server.support.dispatch.actor.result.NoRetryActor;
import com.x.retry.server.support.dispatch.actor.scan.ScanGroupActor;

/**
 * Actor生成器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-30 11:14
 */
public class ActorGenerator {

    private ActorGenerator() {}

    /**
     * 生成重试完成的actor
     *
     * @return actor 引用
     */
    public static ActorRef finishActor() {
       return getDispatchResultActorSystem().actorOf(getSpringExtension().props(FinishActor.BEAN_NAME));
    }

    /**
     * 生成重试失败的actor
     *
     * @return actor 引用
     */
    public static ActorRef failureActor() {
        return getDispatchResultActorSystem().actorOf(getSpringExtension().props(FailureActor.BEAN_NAME));
    }

    /**
     * 不触发重试actor
     *
     * @return actor 引用
     */
    public static ActorRef noRetryActor() {
        return getDispatchResultActorSystem().actorOf(getSpringExtension().props(NoRetryActor.BEAN_NAME));
    }

    /**
     * 生成重试执行的actor
     *
     * @return actor 引用
     */
    public static ActorRef execUnitActor() {
        return getDispatchExecUnitActorSystem().actorOf(getSpringExtension().props(ExecUnitActor.BEAN_NAME));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanGroupActor() {
        return getDispatchRetryActorSystem().actorOf(getSpringExtension().props(ScanGroupActor.BEAN_NAME));
    }

    public static SpringExtension getSpringExtension() {
       return SpringContext.getBeanByType(SpringExtension.class);
    }

    /**
     * 重试任务提取器
     * @return
     */
    public static ActorSystem getDispatchRetryActorSystem() {
        return SpringContext.getBean("dispatchRetryActorSystem", ActorSystem.class);
    }

    /**
     * 重试任务分发器
     * @return
     */
    public static ActorSystem getDispatchExecUnitActorSystem() {
        return SpringContext.getBean("dispatchExecUnitActorSystem", ActorSystem.class);
    }
    /**
     * 重试任务结果分发器
     * @return
     */
    public static ActorSystem getDispatchResultActorSystem() {
        return SpringContext.getBean("dispatchResultActorSystem", ActorSystem.class);
    }

}
