package com.aizuda.easy.retry.client.core.intercepter;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.core.RetrySiteSnapshotContext;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.core.loader.EasyRetrySpiLoader;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.model.EasyRetryHeaders;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

/**
 * 重试现场记录器
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-03 13:42
 */
public class RetrySiteSnapshot {

    /**
     * 重试阶段，1-内存重试阶段，2-服务端重试阶段
     */
    private static final RetrySiteSnapshotContext<Integer> RETRY_STAGE = EasyRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 标记重试方法入口
     */
    private static final RetrySiteSnapshotContext<String> RETRY_CLASS_METHOD_ENTRANCE = EasyRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 重试状态
     */
    private static final RetrySiteSnapshotContext<Integer> RETRY_STATUS = EasyRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 重试请求头
     */
    private static final RetrySiteSnapshotContext<EasyRetryHeaders> RETRY_HEADER =  EasyRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 状态码
     */
    private static final RetrySiteSnapshotContext<String> RETRY_STATUS_CODE =  EasyRetrySpiLoader.loadRetrySiteSnapshotContext();

    /**
     * 进入方法入口时间标记
     */
    private static final RetrySiteSnapshotContext<Long> ENTRY_METHOD_TIME =  EasyRetrySpiLoader.loadRetrySiteSnapshotContext();
    private static final RetrySiteSnapshotContext<Integer> ATTEMPT_NUMBER =  EasyRetrySpiLoader.loadRetrySiteSnapshotContext();

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
        return RETRY_CLASS_METHOD_ENTRANCE.get();
    }

    public static void setMethodEntrance(String methodEntrance) {
        RETRY_CLASS_METHOD_ENTRANCE.set(methodEntrance);
    }

    public static void removeMethodEntrance() {
        RETRY_CLASS_METHOD_ENTRANCE.remove();
    }

    public static boolean isMethodEntrance(String methodEntrance) {
        if (StrUtil.isBlank(getMethodEntrance())) {
            return false;
        }

        return getMethodEntrance().equals(methodEntrance);
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

    public static EasyRetryHeaders getRetryHeader() {
        return RETRY_HEADER.get();
    }

    public static void setRetryHeader(EasyRetryHeaders headers) {
        RETRY_HEADER.set(headers);
    }

    /**
     * 是否是重试流量
     */
    public static boolean isRetryFlow() {
        EasyRetryHeaders retryHeader = getRetryHeader();
        if (Objects.nonNull(retryHeader)) {
            return retryHeader.isEasyRetry();
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
            .equals(SystemConstants.EASY_RETRY_STATUS_CODE);
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
        removeMethodEntrance();
        removeStage();
        removeAttemptNumber();
        removeEntryMethodTime();
        removeRetryHeader();
        removeRetryStatusCode();
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

            throw new EasyRetryClientException("unsupported stage");
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

}
