package com.aizuda.easy.retry.server.job.task;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author www.byteblogs.com
 * @date 2023-09-29 23:29:44
 * @since 2.4.0
 */
@Configuration
@ComponentScan("com.aizuda.easy.retry.server.job.task.*")
@ConditionalOnExpression("'${easy-retry.mode}'.equals('job') or '${easy-retry.mode}'.equals('all')")
public class EasyRetryJobTaskStarter  {

}
