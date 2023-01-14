package com.x.retry.client.starter;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author: www.byteblogs.com
 * @date : 2021-12-31 18:45
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({XRetryClientConfig.class, XRetryClientsRegistrar.class})
public @interface EnableXRetry {

    String group();
}
