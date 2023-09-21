package com.aizuda.easy.retry.server.common.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.aizuda.easy.retry.common.core.context.SpringContext;


/**
 * Actor生成器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-30 11:14
 */
public class ActorGenerator {

    public static final String SCAN_CALLBACK_GROUP_ACTOR = "ScanCallbackGroupActor";
    public static final String SCAN_RETRY_GROUP_ACTOR = "ScanGroupActor";
    public static final String SCAN_BUCKET_ACTOR = "ScanBucketActor";
    public static final String FINISH_ACTOR = "FinishActor";
    public static final String FAILURE_ACTOR = "FailureActor";
    public static final String NO_RETRY_ACTOR = "NoRetryActor";
    public static final String EXEC_CALLBACK_UNIT_ACTOR = "ExecCallbackUnitActor";
    public static final String EXEC_UNIT_ACTOR = "ExecUnitActor";
    public static final String LOG_ACTOR = "LogActor";
    public static final String REQUEST_HANDLER_ACTOR = "RequestHandlerActor";


    private ActorGenerator() {}

    /**
     * 生成重试完成的actor
     *
     * @return actor 引用
     */
    public static ActorRef finishActor() {
       return getDispatchResultActorSystem().actorOf(getSpringExtension().props(FINISH_ACTOR));
    }

    /**
     * 生成重试失败的actor
     *
     * @return actor 引用
     */
    public static ActorRef failureActor() {
        return getDispatchResultActorSystem().actorOf(getSpringExtension().props(FAILURE_ACTOR));
    }

    /**
     * 不触发重试actor
     *
     * @return actor 引用
     */
    public static ActorRef noRetryActor() {
        return getDispatchResultActorSystem().actorOf(getSpringExtension().props(NO_RETRY_ACTOR));
    }

    /**
     * 回调处理
     *
     * @return actor 引用
     */
    public static ActorRef execCallbackUnitActor() {
        return getDispatchResultActorSystem().actorOf(getSpringExtension().props(EXEC_CALLBACK_UNIT_ACTOR));
    }

    /**
     * 生成重试执行的actor
     *
     * @return actor 引用
     */
    public static ActorRef execUnitActor() {
        return getDispatchExecUnitActorSystem().actorOf(getSpringExtension().props(EXEC_UNIT_ACTOR));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanGroupActor() {
        return getDispatchRetryActorSystem().actorOf(getSpringExtension().props(SCAN_RETRY_GROUP_ACTOR));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanCallbackGroupActor() {
        return getDispatchRetryActorSystem().actorOf(getSpringExtension().props(SCAN_CALLBACK_GROUP_ACTOR));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanBucketActor() {
        return getDispatchRetryActorSystem().actorOf(getSpringExtension().props(SCAN_BUCKET_ACTOR));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef logActor() {
        return getLogActorSystemSystem().actorOf(getSpringExtension().props(LOG_ACTOR));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef requestHandlerActor() {
        return getNettyActorSystem().actorOf(getSpringExtension().props(REQUEST_HANDLER_ACTOR));
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

    /**
     * 日志记录分发器
     *
     * @return
     */
    public static ActorSystem getLogActorSystemSystem() {
        return SpringContext.getBean("logActorSystem", ActorSystem.class);
    }


    /**
     * 处理netty客户端请求
     *
     * @return
     */
    public static ActorSystem getNettyActorSystem() {
        return SpringContext.getBean("nettyActorSystem", ActorSystem.class);
    }
}
