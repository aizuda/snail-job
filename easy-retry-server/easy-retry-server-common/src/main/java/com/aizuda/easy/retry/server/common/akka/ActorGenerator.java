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

    /*----------------------------------------系统通用配置 START----------------------------------------*/

    public static final String SCAN_BUCKET_ACTOR = "ScanBucketActor";
    public static final String REQUEST_HANDLER_ACTOR = "RequestHandlerActor";
    private static final String COMMON_LOG_DISPATCHER = "akka.actor.common-log-dispatcher";
    private static final String COMMON_SCAN_TASK_DISPATCHER = "akka.actor.common-scan-task-dispatcher";
    private static final String NETTY_RECEIVE_REQUEST_DISPATCHER = "akka.actor.netty-receive-request-dispatcher";

    /*----------------------------------------系统通用配置 END----------------------------------------*/

    /*----------------------------------------分布式重试任务 START----------------------------------------*/
    public static final String SCAN_CALLBACK_GROUP_ACTOR = "ScanCallbackGroupActor";
    public static final String SCAN_RETRY_GROUP_ACTOR = "ScanGroupActor";
    public static final String FINISH_ACTOR = "FinishActor";
    public static final String FAILURE_ACTOR = "FailureActor";
    public static final String NO_RETRY_ACTOR = "NoRetryActor";
    public static final String EXEC_CALLBACK_UNIT_ACTOR = "ExecCallbackUnitActor";
    public static final String EXEC_UNIT_ACTOR = "ExecUnitActor";
    public static final String LOG_ACTOR = "RetryLogActor";

    private static final String RETRY_TASK_EXECUTOR_DISPATCHER = "akka.actor.retry-task-executor-dispatcher";
    private static final String RETRY_TASK_EXECUTOR_RESULT_DISPATCHER = "akka.actor.retry-task-executor-result-dispatcher";

    /*----------------------------------------分布式重试任务 END----------------------------------------*/

    /*----------------------------------------分布式任务调度 START----------------------------------------*/
    public static final String SCAN_JOB_ACTOR = "ScanJobActor";
    public static final String SCAN_WORKFLOW_ACTOR = "ScanWorkflowTaskActor";
    public static final String JOB_TASK_PREPARE_ACTOR = "JobTaskPrepareActor";
    public static final String WORKFLOW_TASK_PREPARE_ACTOR = "WorkflowTaskPrepareActor";
    public static final String JOB_EXECUTOR_ACTOR = "JobExecutorActor";
    public static final String WORKFLOW_EXECUTOR_ACTOR = "WorkflowExecutorActor";
    public static final String JOB_EXECUTOR_RESULT_ACTOR = "JobExecutorResultActor";
    public static final String JOB_LOG_ACTOR = "JobLogActor";
    public static final String REAL_JOB_EXECUTOR_ACTOR = "RealJobExecutorActor";
    public static final String REAL_STOP_TASK_INSTANCE_ACTOR = "RealStopTaskInstanceActor";

    /*----------------------------------------dispatcher----------------------------------------*/
    private static final String JOB_TASK_DISPATCHER = "akka.actor.job-task-prepare-dispatcher";
    private static final String JOB_TASK_EXECUTOR_DISPATCHER = "akka.actor.job-task-executor-dispatcher";
    private static final String JOB_TASK_EXECUTOR_RESULT_DISPATCHER = "akka.actor.job-task-executor-result-dispatcher";
    private static final String JOB_TASK_EXECUTOR_CALL_CLIENT_DISPATCHER = "akka.actor.job-task-executor-call-client-dispatcher";
    private static final String WORKFLOW_TASK_DISPATCHER = "akka.actor.workflow-task-prepare-dispatcher";
    private static final String WORKFLOW_TASK_EXECUTOR_DISPATCHER = "akka.actor.workflow-task-executor-dispatcher";

    /*----------------------------------------分布式任务调度 END----------------------------------------*/

    private ActorGenerator() {}

    /**
     * 生成重试完成的actor
     *
     * @return actor 引用
     */
    public static ActorRef finishActor() {
       return getRetryActorSystem().actorOf(getSpringExtension().props(FINISH_ACTOR).withDispatcher(RETRY_TASK_EXECUTOR_RESULT_DISPATCHER));
    }

    /**
     * 生成重试失败的actor
     *
     * @return actor 引用
     */
    public static ActorRef failureActor() {
        return getRetryActorSystem().actorOf(getSpringExtension().props(FAILURE_ACTOR).withDispatcher(RETRY_TASK_EXECUTOR_RESULT_DISPATCHER));
    }

    /**
     * 不触发重试actor
     *
     * @return actor 引用
     */
    public static ActorRef noRetryActor() {
        return getRetryActorSystem().actorOf(getSpringExtension().props(NO_RETRY_ACTOR).withDispatcher(RETRY_TASK_EXECUTOR_RESULT_DISPATCHER));
    }

    /**
     * 回调处理
     *
     * @return actor 引用
     */
    public static ActorRef execCallbackUnitActor() {
        return getRetryActorSystem().actorOf(getSpringExtension()
                .props(EXEC_CALLBACK_UNIT_ACTOR)
                .withDispatcher(RETRY_TASK_EXECUTOR_DISPATCHER));
    }

    /**
     * 生成重试执行的actor
     *
     * @return actor 引用
     */
    public static ActorRef execUnitActor() {
        return getRetryActorSystem().actorOf(getSpringExtension()
            .props(EXEC_UNIT_ACTOR)
            .withDispatcher(RETRY_TASK_EXECUTOR_DISPATCHER));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanGroupActor() {
        return getCommonActorSystemSystem().actorOf(getSpringExtension()
            .props(SCAN_RETRY_GROUP_ACTOR)
            .withDispatcher(COMMON_SCAN_TASK_DISPATCHER));
    }

    /**
     * 生成扫描回调数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanCallbackGroupActor() {
        return getRetryActorSystem().actorOf(getSpringExtension()
            .props(SCAN_CALLBACK_GROUP_ACTOR)
            .withDispatcher(COMMON_SCAN_TASK_DISPATCHER));
    }

    /**
     * 生成扫描JOB任务的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanJobActor() {
        return getCommonActorSystemSystem().actorOf(getSpringExtension()
            .props(SCAN_JOB_ACTOR)
            .withDispatcher(COMMON_SCAN_TASK_DISPATCHER));
    }

    /**
     * 生成扫描工作流任务的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanWorkflowActor() {
        return getCommonActorSystemSystem().actorOf(getSpringExtension()
                .props(SCAN_WORKFLOW_ACTOR)
                .withDispatcher(COMMON_SCAN_TASK_DISPATCHER));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanBucketActor() {
        return getCommonActorSystemSystem().actorOf(getSpringExtension()
            .props(SCAN_BUCKET_ACTOR)
            .withDispatcher(COMMON_SCAN_TASK_DISPATCHER));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef logActor() {
        return getCommonActorSystemSystem().actorOf(getSpringExtension()
            .props(LOG_ACTOR)
            .withDispatcher(COMMON_LOG_DISPATCHER));
    }

    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef requestHandlerActor() {
        return getNettyActorSystem().actorOf(getSpringExtension().props(REQUEST_HANDLER_ACTOR)
            .withDispatcher(NETTY_RECEIVE_REQUEST_DISPATCHER));
    }


    /**
     * Job调度准备阶段actor
     *
     * @return actor 引用
     */
    public static ActorRef jobTaskPrepareActor() {
        return getJobActorSystem().actorOf(getSpringExtension().props(JOB_TASK_PREPARE_ACTOR)
                .withDispatcher(JOB_TASK_DISPATCHER));
    }

    /**
     * Job调度准备阶段actor
     *
     * @return actor 引用
     */
    public static ActorRef workflowTaskPrepareActor() {
        return getJobActorSystem().actorOf(getSpringExtension().props(WORKFLOW_TASK_PREPARE_ACTOR)
            .withDispatcher(WORKFLOW_TASK_DISPATCHER));
    }

    /**
     * Job任务执行阶段actor
     *
     * @return actor 引用
     */
    public static ActorRef jobTaskExecutorActor() {
        return getJobActorSystem()
                .actorOf(getSpringExtension()
                        .props(JOB_EXECUTOR_ACTOR)
                        .withDispatcher(JOB_TASK_EXECUTOR_DISPATCHER)
                );
    }

    /**
     * Job任务执行阶段actor
     *
     * @return actor 引用
     */
    public static ActorRef workflowTaskExecutorActor() {
        return getJobActorSystem()
            .actorOf(getSpringExtension()
                .props(WORKFLOW_EXECUTOR_ACTOR)
                .withDispatcher(WORKFLOW_TASK_EXECUTOR_DISPATCHER)
            );
    }

    /**
     * Job任务执行结果actor
     *
     * @return actor 引用
     */
    public static ActorRef jobTaskExecutorResultActor() {
        return getJobActorSystem().actorOf(getSpringExtension()
                .props(JOB_EXECUTOR_RESULT_ACTOR)
                .withDispatcher(JOB_TASK_EXECUTOR_RESULT_DISPATCHER));
    }

    /**
     * Job任务向客户端发起请求阶段actor
     *
     * @return actor 引用
     */
    public static ActorRef jobRealTaskExecutorActor() {
        return getJobActorSystem().actorOf(getSpringExtension()
            .props(REAL_JOB_EXECUTOR_ACTOR)
            .withDispatcher(JOB_TASK_EXECUTOR_CALL_CLIENT_DISPATCHER));
    }

    /**
     * Job任务向客户端发起请求阶段actor
     *
     * @return actor 引用
     */
    public static ActorRef jobRealStopTaskInstanceActor() {
        return getJobActorSystem().actorOf(getSpringExtension().props(REAL_STOP_TASK_INSTANCE_ACTOR)
            .withDispatcher(JOB_TASK_EXECUTOR_CALL_CLIENT_DISPATCHER));
    }

    /**
     * Job日志actor
     *
     * @return actor 引用
     */
    public static ActorRef jobLogActor() {
        return getCommonActorSystemSystem().actorOf(getSpringExtension().props(JOB_LOG_ACTOR)
            .withDispatcher(COMMON_LOG_DISPATCHER));
    }

    public static SpringExtension getSpringExtension() {
       return SpringContext.getBeanByType(SpringExtension.class);
    }

   /**
     * 重试任务结果分发器
     * @return
     */
    public static ActorSystem getRetryActorSystem() {
        return SpringContext.getBean("retryActorSystem", ActorSystem.class);
    }

    /**
     * 日志记录分发器
     *
     * @return
     */
    public static ActorSystem getCommonActorSystemSystem() {
        return SpringContext.getBean("commonActorSystem", ActorSystem.class);
    }


    /**
     * 处理netty客户端请求
     *
     * @return
     */
    public static ActorSystem getNettyActorSystem() {
        return SpringContext.getBean("nettyActorSystem", ActorSystem.class);
    }

    /**
     * 处理Job调度
     *
     * @return
     */
    public static ActorSystem getJobActorSystem() {
        return SpringContext.getBean("jobActorSystem", ActorSystem.class);
    }

}
