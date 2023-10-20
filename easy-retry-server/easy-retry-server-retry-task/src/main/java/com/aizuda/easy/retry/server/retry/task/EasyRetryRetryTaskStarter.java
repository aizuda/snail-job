package com.aizuda.easy.retry.server.retry.task;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-12 08:54
 * @since : 2.4.0
 */
@Configuration
@ComponentScan("com.aizuda.easy.retry.server.retry.task.*")
@ConditionalOnExpression("'${easy-retry.mode}'.equals('retry') or '${easy-retry.mode}'.equals('all')")
public class EasyRetryRetryTaskStarter {

}
