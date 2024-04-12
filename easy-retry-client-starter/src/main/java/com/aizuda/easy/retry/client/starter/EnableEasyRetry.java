package com.aizuda.easy.retry.client.starter;

import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.annotation.*;

/**
 * 在启动类上添加EnableEasyRetry注解开启Easy Retry功能
 *
 * @author: opensnail
 * @date : 2021-12-31 18:45
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EasyRetryClientsRegistrar.class)
public @interface EnableEasyRetry {

    /**
     * 表示该重试数据属于哪个系统并且全局唯一
     *
     * @return
     */
    String group();

    /**
     * 控制多个Aop的执行顺序,
     * 需要注意的是这里顺序要比事务的Aop要提前
     *
     * see {@link  EnableTransactionManagement#order()}
     * 默认值: Ordered.HIGHEST_PRECEDENCE
     */
    int order() default Ordered.HIGHEST_PRECEDENCE;


}
