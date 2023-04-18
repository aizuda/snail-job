package com.aizuda.easy.retry.common.core.util;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-16 14:37
 */
public class Assert {

    public static <T extends RuntimeException> void isTrue(boolean expression, T e) {
        if (!expression) {
            throw e;
        }
    }
}
