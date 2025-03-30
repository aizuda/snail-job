package com.aizuda.snailjob.client.common.util;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

public class ValidatorUtils {
    private static final Validator validator;

    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public static Pair<Boolean, String> validateEntity(Object object) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
        return validateEntity(constraintViolations, object);
    }

    public static Pair<Boolean, String> validateEntity(Class<?> group, Object object) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, group);
        return validateEntity(constraintViolations, object);
    }

    /**
     * 校验对象
     *
     * @param object 待校验对象
     * @throws SnailJobClientException 校验不通过，则报SnailJobClientException异常
     */
    public static Pair<Boolean, String> validateEntity( Set<ConstraintViolation<Object>> constraintViolations, Object object) {
        if (!constraintViolations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            for (ConstraintViolation<Object> constraint : constraintViolations) {
                msg.append(constraint.getMessage()).append("\n");
            }
            return Pair.of(Boolean.FALSE, msg.toString());
        } else {
            return Pair.of(Boolean.TRUE, null);
        }
    }
}