package com.aizuda.snailjob.server.common.pekko;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;


/**
 * Actor生成器
 *
 * @author: opensnail
 * @date : 2021-11-30 11:14
 */
public class ActorGenerator {

    /*----------------------------------------系统通用配置 START----------------------------------------*/

    public static final String SCAN_BUCKET_ACTOR = "ScanBucketActor";
    public static final String REQUEST_HANDLER_ACTOR = "RequestHandlerActor";
    public static final String GRPC_REQUEST_HANDLER_ACTOR = "GrpcRequestHandlerActor";
    private static final String COMMON_LOG_DISPATCHER = "pekko.actor.common-log-dispatcher";
    private static final String COMMON_SCAN_TASK_DISPATCHER = "pekko.actor.common-scan-task-dispatcher";
    private static final String NETTY_RECEIVE_REQUEST_DISPATCHER = "pekko.actor.netty-receive-request-dispatcher";

    /*----------------------------------------系统通用配置 END----------------------------------------*/

    /*----------------------------------------分布式重试任务 START----------------------------------------*/
    public static final String SCAN_CALLBACK_GROUP_ACTOR = "ScanCallbackGroupActor";
    public static final String SCAN_RETRY_ACTOR = "ScanRetryActor";
    public static final String RETRY_EXECUTOR_ACTOR = "RetryExecutorActor";
    public static final String RETRY_TASK_PREPARE_ACTOR = "RetryTaskPrepareActor";
    public static final String LOG_ACTOR = "RetryLogActor";
    public static final String RETRY_EXECUTOR_RESULT_ACTOR = "RetryExecutorResultActor";
    public static final String REAL_RETRY_EXECUTOR_ACTOR = "RealRetryExecutorActor";
    public static final String REAL_CALLBACK_EXECUTOR_ACTOR = "RealCallbackExecutorActor";
    public static final String RETRY_REAL_STOP_TASK_INSTANCE_ACTOR = "RetryRealStopTaskInstanceActor";


    private static final String RETRY_TASK_EXECUTOR_DISPATCHER = "pekko.actor.retry-task-executor-dispatcher";
    private static final String RETRY_TASK_EXECUTOR_RESULT_DISPATCHER = "pekko.actor.retry-task-executor-result-dispatcher";
    private static final String RETRY_TASK_EXECUTOR_CALL_CLIENT_DISPATCHER = "pekko.actor.retry-task-executor-call-client-dispatcher";

    /*----------------------------------------分布式重试任务 END----------------------------------------*/

    /*----------------------------------------分布式任务调度 START----------------------------------------*/
    public static final String SCAN_JOB_ACTOR = "ScanJobActor";
    public static final String SCAN_WORKFLOW_ACTOR = "ScanWorkflowTaskActor";
    public static final String JOB_TASK_PREPARE_ACTOR = "JobTaskPrepareActor";
    public static final String JOB_REDUCE_ACTOR = "JobReduceActor";
    public static final String WORKFLOW_TASK_PREPARE_ACTOR = "WorkflowTaskPrepareActor";
    public static final String JOB_EXECUTOR_ACTOR = "JobExecutorActor";
    public static final String WORKFLOW_EXECUTOR_ACTOR = "WorkflowExecutorActor";
    public static final String JOB_EXECUTOR_RESULT_ACTOR = "JobExecutorResultActor";
    public static final String JOB_LOG_ACTOR = "JobLogActor";
    public static final String REAL_JOB_EXECUTOR_ACTOR = "RealJobExecutorActor";
    public static final String JOB_REAL_STOP_TASK_INSTANCE_ACTOR = "JobRealStopTaskInstanceActor";

    /*----------------------------------------dispatcher----------------------------------------*/
    private static final String JOB_TASK_DISPATCHER = "pekko.actor.job-task-prepare-dispatcher";
    private static final String JOB_TASK_EXECUTOR_DISPATCHER = "pekko.actor.job-task-executor-dispatcher";
    private static final String JOB_TASK_EXECUTOR_RESULT_DISPATCHER = "pekko.actor.job-task-executor-result-dispatcher";
    private static final String JOB_TASK_EXECUTOR_CALL_CLIENT_DISPATCHER = "pekko.actor.job-task-executor-call-client-dispatcher";
    private static final String WORKFLOW_TASK_DISPATCHER = "pekko.actor.workflow-task-prepare-dispatcher";
    private static final String WORKFLOW_TASK_EXECUTOR_DISPATCHER = "pekko.actor.workflow-task-executor-dispatcher";

    /*----------------------------------------分布式任务调度 END----------------------------------------*/

    private ActorGenerator() {
    }

    /**
     * Retry任务执行结果actor
     *
     * @return actor 引用
     */
    public static ActorRef retryTaskExecutorResultActor() {
        return getJobActorSystem().actorOf(getSpringExtension()
                .props(RETRY_EXECUTOR_RESULT_ACTOR)
                .withDispatcher(RETRY_TASK_EXECUTOR_RESULT_DISPATCHER));
    }


    /**
     * 生成扫描重试数据的actor
     *
     * @return actor 引用
     */
    public static ActorRef scanRetryActor() {
        return getCommonActorSystemSystem().actorOf(getSpringExtension()
                .props(SCAN_RETRY_ACTOR)
                .withDispatcher(COMMON_SCAN_TASK_DISPATCHER));
    }

    /**
     * actor
     *
     * @return actor 引用
     */
    public static ActorRef retryTaskExecutorActor() {
        return getCommonActorSystemSystem().actorOf(getSpringExtension()
                .props(RETRY_EXECUTOR_ACTOR)
                .withDispatcher(COMMON_SCAN_TASK_DISPATCHER));
    }

    /**
     * actor
     *
     * @return actor 引用
     */
    public static ActorRef retryTaskPrepareActor() {
        return getCommonActorSystemSystem().actorOf(getSpringExtension()
                .props(RETRY_TASK_PREPARE_ACTOR)
                .withDispatcher(RETRY_TASK_EXECUTOR_DISPATCHER));
    }

    /**
     * 尝试停止执行中的任务
     *
     * @return ActorRef
     */
    public static ActorRef stopRetryTaskActor() {
        return getRetryActorSystem().actorOf(getSpringExtension()
                .props(RETRY_REAL_STOP_TASK_INSTANCE_ACTOR)
                .withDispatcher(RETRY_TASK_EXECUTOR_CALL_CLIENT_DISPATCHER));
    }

    /**
     * 调用客户端执行重试
     *
     * @return ActorRef
     */
    public static ActorRef retryRealTaskExecutorActor() {
        return getRetryActorSystem().actorOf(getSpringExtension()
                .props(REAL_RETRY_EXECUTOR_ACTOR)
                .withDispatcher(RETRY_TASK_EXECUTOR_CALL_CLIENT_DISPATCHER));
    }

    /**
     * 调用客户端执行回调
     *
     * @return ActorRef
     */
    public static ActorRef callbackRealTaskExecutorActor() {
        return getRetryActorSystem().actorOf(getSpringExtension()
                .props(REAL_CALLBACK_EXECUTOR_ACTOR)
                .withDispatcher(RETRY_TASK_EXECUTOR_CALL_CLIENT_DISPATCHER));
    }




    /**
     * 生成扫描回调数据的actor
     *
     * @return actor 引用
     */
    @Deprecated
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
    @Deprecated
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
     * netty请求处理器
     *
     * @return actor 引用
     */
    public static ActorRef requestHandlerActor() {
        return getNettyActorSystem().actorOf(getSpringExtension().props(REQUEST_HANDLER_ACTOR)
                .withDispatcher(NETTY_RECEIVE_REQUEST_DISPATCHER));
    }

    /**
     * Grpc请求处理器
     *
     * @return actor 引用
     */
    public static ActorRef requestGrpcHandlerActor() {
        return getNettyActorSystem().actorOf(getSpringExtension().props(GRPC_REQUEST_HANDLER_ACTOR)
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
     * 动态分片任务处理reduce阶段actor
     *
     * @return actor 引用
     */
    public static ActorRef jobReduceActor() {
        return getJobActorSystem().actorOf(getSpringExtension().props(JOB_REDUCE_ACTOR)
                .withDispatcher(JOB_TASK_EXECUTOR_DISPATCHER));
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
        return getJobActorSystem().actorOf(getSpringExtension().props(JOB_REAL_STOP_TASK_INSTANCE_ACTOR)
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
        return SnailSpringContext.getBeanByType(SpringExtension.class);
    }

    /**
     * 重试任务结果分发器
     *
     * @return
     */
    public static ActorSystem getRetryActorSystem() {
        return SnailSpringContext.getBean("retryActorSystem", ActorSystem.class);
    }

    /**
     * 日志记录分发器
     *
     * @return
     */
    public static ActorSystem getCommonActorSystemSystem() {
        return SnailSpringContext.getBean("commonActorSystem", ActorSystem.class);
    }


    /**
     * 处理netty客户端请求
     *
     * @return
     */
    public static ActorSystem getNettyActorSystem() {
        return SnailSpringContext.getBean("nettyActorSystem", ActorSystem.class);
    }

    /**
     * 处理Job调度
     *
     * @return
     */
    public static ActorSystem getJobActorSystem() {
        return SnailSpringContext.getBean("jobActorSystem", ActorSystem.class);
    }

}
