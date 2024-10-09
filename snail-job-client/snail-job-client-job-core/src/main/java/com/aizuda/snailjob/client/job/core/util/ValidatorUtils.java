package com.aizuda.snailjob.client.job.core.util;

import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.common.log.SnailJobLog;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

public class ValidatorUtils {
    private static Validator validator;

    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * 校验对象
     * @param object        待校验对象
     * @throws SnailJobClientException  校验不通过，则报SnailJobClientException异常
     */
    public static boolean validateEntity(Object object)
            throws SnailJobClientException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
        if (!constraintViolations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            for(ConstraintViolation<Object> constraint:  constraintViolations){
                msg.append(constraint.getMessage()).append("\n");
            }
            SnailJobLog.LOCAL.error(msg.toString());
            return false;
        }else {
            return true;
        }
    }
}