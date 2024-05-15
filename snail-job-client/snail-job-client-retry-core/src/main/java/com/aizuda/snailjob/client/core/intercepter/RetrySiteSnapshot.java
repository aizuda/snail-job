package com.aizuda.snailjob.client.core.intercepter;

import com.aizuda.snailjob.client.core.RetrySiteSnapshotContext;
import com.aizuda.snailjob.client.core.exception.SnailRetryClientException;
import com.aizuda.snailjob.client.core.loader.SnailRetrySpiLoader;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.model.SnailJobHeaders;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 重试现场记录器
 *
 * @author: opensnail
 * @date : 2022-03-03 13:42
 */
public class RetrySiteSnapshot {

    /**
     * 重试阶段，1-内存重试阶段，2-服务端重试阶段
     */
    private static final RetrySiteSnapshotContext<Integer> RETRY_STAGE = SnailRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 标记重试方法入口
     */
    private static final RetrySiteSnapshotContext<Deque<MethodEntranceMeta>> RETRY_CLASS_METHOD_ENTRANCE = SnailRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 重试状态
     */
    private static final RetrySiteSnapshotContext<Integer> RETRY_STATUS = SnailRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 重试请求头
     */
    private static final RetrySiteSnapshotContext<SnailJobHeaders> RETRY_HEADER = SnailRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 状态码
     */
    private static final RetrySiteSnapshotContext<String> RETRY_STATUS_CODE = SnailRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 进入方法入口时间标记
     */
    private static final RetrySiteSnapshotContext<Long> ENTRY_METHOD_TIME = SnailRetrySpiLoader.loadRetrySiteSnapshotContext();
    private static final RetrySiteSnapshotContext<Integer> ATTEMPT_NUMBER = SnailRetrySpiLoader.loadRetrySiteSnapshotContext();

    public static Integer getAttemptNumber() {
        return ATTEMPT_NUMBER.get();
    }

    public static void setAttemptNumber(Integer attemptNumber) {
        ATTEMPT_NUMBER.set(attemptNumber);
    }

    public static void removeAttemptNumber() {
        ATTEMPT_NUMBER.remove();
    }

    public static Integer getStage() {
        return RETRY_STAGE.get();
    }

    public static void setStage(int stage) {
        RETRY_STAGE.set(stage);
    }

    public static String getMethodEntrance() {
        Deque<MethodEntranceMeta> stack = RETRY_CLASS_METHOD_ENTRANCE.get();
        if (Objects.isNull(stack) || Objects.isNull(stack.peek())) {
            return null;
        }

        return stack.peek().methodEntrance;
    }

    public static boolean existedMethodEntrance() {
        Deque<MethodEntranceMeta> stack = RETRY_CLASS_METHOD_ENTRANCE.get();
        if (Objects.isNull(stack)) {
            return Boolean.FALSE;
        }

        MethodEntranceMeta meta = stack.peek();
        if (Objects.isNull(meta)) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    public static void setMethodEntrance(String methodEntrance) {
        Deque<MethodEntranceMeta> stack = RETRY_CLASS_METHOD_ENTRANCE.get();
        if (Objects.isNull(RETRY_CLASS_METHOD_ENTRANCE.get())) {
            stack = new LinkedBlockingDeque<>();
            RETRY_CLASS_METHOD_ENTRANCE.set(stack);
        }

        MethodEntranceMeta meta;
        if (!isRunning() && !isRetryFlow()) {
            meta = new MethodEntranceMeta(methodEntrance);
            stack.push(meta);
        }
    }

    public static void removeMethodEntrance() {
        Deque<MethodEntranceMeta> stack = RETRY_CLASS_METHOD_ENTRANCE.get();
        if (Objects.isNull(stack)) {
            return;
        }

        if (stack.isEmpty()) {
            RETRY_CLASS_METHOD_ENTRANCE.remove();
            return;
        }

        if (!isRunning() && !isRetryFlow()) {
            stack.pop();
        }

    }

    public static boolean isMethodEntrance(String methodEntrance) {
        Deque<MethodEntranceMeta> stack = RETRY_CLASS_METHOD_ENTRANCE.get();
        if (Objects.isNull(stack) || Objects.isNull(stack.peek())) {
            return Boolean.FALSE;
        }

        MethodEntranceMeta peek = stack.peek();
        return methodEntrance.equals(peek.methodEntrance);
    }

    public static Integer getStatus() {
        return Optional.ofNullable(RETRY_STATUS.get()).orElse(EnumStatus.COMPLETE.status);

    }

    public static void setStatus(Integer status) {
        RETRY_STATUS.set(status);
    }

    public static boolean isRunning() {
        return EnumStatus.RUNNING.status == getStatus();
    }

    public static SnailJobHeaders getRetryHeader() {
        return RETRY_HEADER.get();
    }

    public static void setRetryHeader(SnailJobHeaders headers) {
        RETRY_HEADER.set(headers);
    }

    /**
     * 是否是重试流量
     */
    public static boolean isRetryFlow() {
        SnailJobHeaders retryHeader = getRetryHeader();
        if (Objects.nonNull(retryHeader)) {
            return retryHeader.isRetry();
        }

        return false;
    }

    public static String getRetryStatusCode() {
        return RETRY_STATUS_CODE.get();
    }

    public static void setRetryStatusCode(String statusCode) {
        RETRY_STATUS_CODE.set(statusCode);
    }

    public static boolean isRetryForStatusCode() {
        return Objects.nonNull(getRetryStatusCode()) && getRetryStatusCode()
                .equals(SystemConstants.SNAIL_JOB_STATUS_CODE);
    }

    public static Long getEntryMethodTime() {
        return ENTRY_METHOD_TIME.get();
    }

    public static void setEntryMethodTime(long entryMethodTime) {
        ENTRY_METHOD_TIME.set(entryMethodTime);
    }

    public static void removeEntryMethodTime() {
        ENTRY_METHOD_TIME.remove();
    }

    public static void removeRetryHeader() {
        RETRY_HEADER.remove();
    }

    public static void removeRetryStatusCode() {
        RETRY_STATUS_CODE.remove();
    }

    public static void removeStage() {
        RETRY_STAGE.remove();
    }

    public static void removeStatus() {
        RETRY_STATUS.remove();
    }

    public static void removeAll() {

        removeStatus();
        removeStage();
        removeAttemptNumber();
        removeEntryMethodTime();
        removeRetryHeader();
        removeRetryStatusCode();
        removeMethodEntrance();
    }

    /**
     * 重试阶段
     */
    @Getter
    public enum EnumStage {

        /**
         * 本地重试阶段
         */
        LOCAL(1),

        /**
         * 远程重试阶段
         */
        REMOTE(2),

        /**
         * 手动提交数据
         */
        MANUAL_REPORT(3),
        ;

        private final int stage;

        EnumStage(int stage) {
            this.stage = stage;
        }

        public static EnumStage valueOfStage(int stage) {
            for (final EnumStage value : EnumStage.values()) {
                if (value.getStage() == stage) {
                    return value;
                }
            }

            throw new SnailRetryClientException("unsupported stage");
        }

    }

    /**
     * 重试状态
     */
    @Getter
    public enum EnumStatus {

        /**
         * 重试中
         */
        RUNNING(1),

        /**
         * 重试完成
         */
        COMPLETE(2),
        ;

        private final int status;

        EnumStatus(int status) {
            this.status = status;
        }

    }

    @Getter
    @AllArgsConstructor
    public static class MethodEntranceMeta {

//        private AtomicInteger depth;

        private String methodEntrance;
    }

}
