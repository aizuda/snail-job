package com.aizuda.snail.job.server.retry.task.support.event;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryTask;
import org.springframework.context.ApplicationEvent;

/**
 * 重试任务失败数量超过阈值事件
 * @author: zuoJunLin
 * @date : 2023-11-20 21:40
 * @since 2.5.0
 */
public class RetryTaskFailMoreThresholdAlarmEvent extends ApplicationEvent {
   private  RetryTask retryTask;

    public RetryTaskFailMoreThresholdAlarmEvent(RetryTask retryTask) {
        super(retryTask);
        this.retryTask=retryTask;
    }

    public RetryTask getRetryTask() {
        return retryTask;
    }
}
