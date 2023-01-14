package com.x.retry.client.core.intercepter;

import com.x.retry.common.core.model.XRetryHeaders;
import lombok.Getter;

import java.util.Objects;

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
    private static final ThreadLocal<Integer> RETRY_STAGE = new ThreadLocal<>();

    /**
     * 标记重试方法入口
     */
    private static final ThreadLocal<String> RETRY_CLASS_METHOD_ENTRANCE = new ThreadLocal<>();

    /**
     * 重试状态
     */
    private static final ThreadLocal<Integer> RETRY_STATUS = ThreadLocal.withInitial(EnumStatus.COMPLETE::getStatus);

    /**
     * 重试请求头
     */
    private static final ThreadLocal<XRetryHeaders> RETRY_HEADER = new ThreadLocal<>();

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

    public static boolean isMethodEntrance(String methodEntrance) {
        return getMethodEntrance().equals(methodEntrance);
    }

    public static Integer getStatus() {
        return RETRY_STATUS.get();
    }

    public static void setStatus(Integer status) {
        RETRY_STATUS.set(status);
    }

    public static boolean isRunning() {
        return EnumStatus.RUNNING.status == getStatus();
    }

    public static XRetryHeaders getRetryHeader() {
        return RETRY_HEADER.get();
    }

    public static void setRetryHeader(XRetryHeaders headers) {
        RETRY_HEADER.set(headers);
    }

    /**
     * 是否是重试流量
     */
    public static boolean isRetryFlow(XRetryHeaders headers) {
        XRetryHeaders retryHeader = getRetryHeader();
        if (Objects.nonNull(retryHeader)) {
            return retryHeader.isXRetry();
        }

        return false;
    }

    public static void removeAll() {
        RETRY_STATUS.remove();
        RETRY_CLASS_METHOD_ENTRANCE.remove();
        RETRY_STAGE.remove();
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
        ;

        private final int stage;
        EnumStage(int stage) {
            this.stage = stage;
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
