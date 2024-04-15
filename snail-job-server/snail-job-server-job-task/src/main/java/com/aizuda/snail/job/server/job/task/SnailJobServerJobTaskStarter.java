package com.aizuda.snail.job.server.job.task;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author opensnail
 * @date 2023-09-29 23:29:44
 * @since 2.4.0
 */
@Configuration
@ComponentScan("com.aizuda.snail.job.server.job.task.*")
@ConditionalOnExpression("'${snail-job.mode}'.equals('job') or '${snail-job.mode}'.equals('all')")
public class SnailJobServerJobTaskStarter {

}
