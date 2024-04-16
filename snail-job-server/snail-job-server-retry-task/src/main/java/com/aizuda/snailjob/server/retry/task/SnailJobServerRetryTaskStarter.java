package com.aizuda.snailjob.server.retry.task;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: opensnail
 * @date : 2023-10-12 08:54
 * @since : 2.4.0
 */
@Configuration
@ComponentScan("com.aizuda.snailjob.server.retry.task.*")
@ConditionalOnExpression("'${snail-job.mode}'.equals('retry') or '${snail-job.mode}'.equals('all')")
public class SnailJobServerRetryTaskStarter {

}
