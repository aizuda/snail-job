package com.aizuda.easy.retry.server.support.strategy;

import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.common.core.enums.RetryResultStatusEnum;
import com.aizuda.easy.retry.server.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.support.RetryContext;
import com.aizuda.easy.retry.server.support.StopStrategy;
import com.aizuda.easy.retry.server.support.context.MaxAttemptsPersistenceRetryContext;

import java.util.Objects;

/**
 * 生成 {@link StopStrategy} 实例.
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 19:22
 */
public class StopStrategies {

    private StopStrategies() {
    }

    /**
     * 调用客户端发生异常触发停止策略
     *
     * @return {@link ExceptionStopStrategy} 调用客户端发生异常触发停止策略
     */
    public static StopStrategy stopException() {
        return new ExceptionStopStrategy();
    }

    /**
     * 根据客户端返回状态判断是否终止重试
     *
     * @return {@link ResultStatusCodeStopStrategy} 重试结果停止策略
     */
    public static StopStrategy stopResultStatus() {
        return new ResultStatusStopStrategy();
    }

    /**
     * 根据客户端返回结果对象的状态码判断是否终止重试
     *
     * @return {@link ResultStatusCodeStopStrategy} 重试结果停止策略
     */
    public static StopStrategy stopResultStatusCode() {
        return new ResultStatusCodeStopStrategy();
    }

    /**
     * 调用客户端发生异常触发停止策略
     */
    private static final class ExceptionStopStrategy implements StopStrategy {

        @Override
        public boolean shouldStop(RetryContext retryContext) {
            return !retryContext.hasException();
        }

        @Override
        public boolean supports(RetryContext retryContext) {
            return true;
        }

        @Override
        public int order() {
            return 1;
        }
    }

    /**
     * 根据客户端返回的状态码判断是否应该停止
     * <p>
     * 1、{@link Result#getStatus()} 不为1 则继续重试
     */
    private static final class ResultStatusStopStrategy implements StopStrategy {

        @Override
        public boolean shouldStop(RetryContext retryContext) {

            Result response = (Result) retryContext.getCallResult();

            if (Objects.isNull(response) || StatusEnum.YES.getStatus() != response.getStatus()) {
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }

        @Override
        public boolean supports(RetryContext retryContext) {
            return true;
        }

        @Override
        public int order() {
            return 2;
        }
    }

    /**
     * 根据客户端返回结果集判断是否应该停止
     * <p>
     * 1、{@link Result#getStatus()} 不为1 则继续重试
     * 2、根据{@link Result#getData()}中的statusCode判断是否停止
     */
    private static final class ResultStatusCodeStopStrategy implements StopStrategy {

        @Override
        public boolean shouldStop(RetryContext retryContext) {

            Result<DispatchRetryResultDTO> response = (Result<DispatchRetryResultDTO>) retryContext.getCallResult();

            if (Objects.isNull(response) || StatusEnum.YES.getStatus() != response.getStatus()) {
                return Boolean.FALSE;
            }

            DispatchRetryResultDTO data = response.getData();
            if (Objects.isNull(data)) {
                return Boolean.FALSE;
            }

            Integer statusCode = data.getStatusCode();
            Integer status = RetryResultStatusEnum.getRetryResultStatusEnum(statusCode).getStatus();
            return RetryResultStatusEnum.SUCCESS.getStatus().equals(status) || RetryResultStatusEnum.STOP.getStatus().equals(status);
        }

        @Override
        public boolean supports(RetryContext retryContext) {
            return retryContext instanceof MaxAttemptsPersistenceRetryContext;
        }

        @Override
        public int order() {
            return 3;
        }
    }

}
