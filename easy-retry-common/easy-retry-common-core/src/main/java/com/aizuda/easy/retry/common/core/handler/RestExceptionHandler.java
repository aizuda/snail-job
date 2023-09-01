package com.aizuda.easy.retry.common.core.handler;

import com.aizuda.easy.retry.common.core.exception.AbstractError;
import com.aizuda.easy.retry.common.core.exception.BaseEasyRetryException;
import com.aizuda.easy.retry.common.core.model.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description: 400 统一异常处理
 * @author: byteblogs
 * @date: 2019/09/30 17:02
 */
@ControllerAdvice(basePackages = {"com.aizuda.easy.retry.client.core", "com.aizuda.easy.retry.server"} )
@Slf4j
@ResponseBody
public class RestExceptionHandler {
    //异常类型
    public static final String DELIMITER_TO = "@";
    public static final String DELIMITER_COLON = ":";

    /**
     * 业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({Exception.class})
    public Result onException(Exception ex) {
        log.error("异常类 onException,", ex);
        return new Result<String>(0, "系统异常");
    }

    /**
     * 业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({BaseEasyRetryException.class})
    public Result onBusinessException(BaseEasyRetryException ex) {
        log.error("异常类 businessException", ex);
        return new Result<String>(0, ex.getMessage());
    }

    /**
     * 400错误
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public Result requestNotReadable(HttpMessageNotReadableException ex) {
        log.error("异常类 HttpMessageNotReadableException,", ex);
        return new Result<String>(0, AbstractError.PARAM_INCORRECT.toString());
    }

    /**
     * validation 异常处理
     *
     * @param e       ConstraintViolationException
     * @return HttpResult
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result onConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        if (!CollectionUtils.isEmpty(constraintViolations)) {
            String errorMessage = constraintViolations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(";"));
            return new Result(0, errorMessage);
        }

        return new Result<String>(0, e.getMessage());
    }

    /**
     * validation 异常处理
     *
     * @param e       MethodArgumentNotValidException
     * @return HttpResult
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        if (result != null && result.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> errors = result.getFieldErrors();
            if(!CollectionUtils.isEmpty(errors)){
                FieldError error = errors.get(0);
                String rejectedValue = Objects.toString(error.getRejectedValue(), "");
                String defMsg = error.getDefaultMessage();
                // 排除类上面的注解提示
                if(rejectedValue.contains(DELIMITER_TO)){
                    // 自己去确定错误字段
                    sb.append(defMsg);
                }else{
                    if(DELIMITER_COLON.contains(defMsg)){
                        sb.append(error.getField()).append(" ").append(defMsg);
                    }else{
                        sb.append(error.getField()).append(" ").append(defMsg).append(":").append(rejectedValue);
                    }
                }
            } else {
                String msg = result.getAllErrors().get(0).getDefaultMessage();
                sb.append(msg);
            }

            return new Result<String>(0, sb.toString());
        }

        return null;
    }

    /**
     * 400错误
     */
    @ExceptionHandler({TypeMismatchException.class})
    public Result requestTypeMismatch(TypeMismatchException ex) {
        log.error("异常类 TypeMismatchException {},", ex.getMessage());
        return new Result<String>(0, AbstractError.PARAM_INCORRECT.toString());
    }

    /**
     * 400错误
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public Result requestMissingServletRequest(MissingServletRequestParameterException ex) {
        log.error("异常类 MissingServletRequestParameterException {},", ex.getMessage());
        return new Result<String>(0,AbstractError.PARAM_INCORRECT.toString());
    }

    /**
     * 405错误
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseBody
    public Result request405() {
        log.error("异常类 HttpRequestMethodNotSupportedException ");
        return new Result<String>(0,AbstractError.PARAM_INCORRECT.toString());
    }

    /**
     * 415错误
     */
    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public Result request415(HttpMediaTypeNotSupportedException ex) {
        log.error("异常类 HttpMediaTypeNotSupportedException {}", ex.getMessage());
        return new Result<String>(0,AbstractError.PARAM_INCORRECT.toString());
    }
}
