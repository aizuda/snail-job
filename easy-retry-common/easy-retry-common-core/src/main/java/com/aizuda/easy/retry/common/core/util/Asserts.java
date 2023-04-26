package com.aizuda.easy.retry.common.core.util;

import com.aizuda.easy.retry.common.core.exception.AbstractError;
import com.aizuda.easy.retry.common.core.exception.BaseEasyRetryException;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class Asserts {

    public static void isTrue(boolean expression, AbstractError serviceError, Object... args) {
        if (!expression) {
//            Class<BaseEasyRetryException> baseXRetryExceptionClass = BaseEasyRetryException.class;
//            Constructor<BaseEasyRetryException> constructor = baseXRetryExceptionClass.getConstructor(String.class, Object[].class);
//            constructor.newInstance(serviceError.toString(), args);
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void isFalse(boolean expression, AbstractError serviceError, Object... args) {
        if (expression) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void isNull(Object object, AbstractError serviceError, Object... args) {
        if (object != null) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void notNull(Object object, AbstractError serviceError, Object... args) {
        if (object == null) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void hasLength(String text, AbstractError serviceError, Object... args) {
        if (!StringUtils.isNotEmpty(text)) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void hasText(String text, AbstractError serviceError, Object... args) {
        if (!StringUtils.isNotBlank(text)) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void isEquals(Object one, Object another, AbstractError serviceError, Object... args) {
        if (!Objects.equals(one, another)) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void notEquals(Object one, Object another, AbstractError serviceError, Object... args) {
        if (Objects.equals(one, another)) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void doesNotContain(String textToSearch, String substring, AbstractError serviceError, Object... args) {
        if (StringUtils.isNotEmpty(textToSearch) && StringUtils.isNotEmpty(substring) &&
                textToSearch.contains(substring)) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void notEmpty(Object[] array, AbstractError serviceError, Object... args) {
        if (array == null || array.length == 0) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void noNullElements(Object[] array, AbstractError serviceError, Object... args) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw new BaseEasyRetryException(serviceError.toString(), args);
                }
            }
        }
    }

    public static void notEmpty(Collection collection, AbstractError serviceError, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void notEmpty(Map map, AbstractError serviceError, Object... args) {
        if (CollectionUtils.isEmpty(map)) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void isInstanceOf(Class<?> type, Object obj, AbstractError serviceError, Object... args) {
        notNull(type, serviceError);
        if (!type.isInstance(obj)) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

    public static void isAssignable(Class<?> superType, Class<?> subType, AbstractError serviceError, Object... args) {
        notNull(superType, serviceError);
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new BaseEasyRetryException(serviceError.toString(), args);
        }
    }

}
