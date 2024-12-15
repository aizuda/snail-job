package com.aizuda.snailjob.server.retry.task.support.event;

import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 重试任务失败数量超过阈值事件
 *
 * @author: zuoJunLin
 * @date : 2023-11-20 21:40
 * @since 2.5.0
 */
@Getter
public class RetryTaskFailMoreThresholdAlarmEvent extends ApplicationEvent {

    private RetryTask retryTask;

    public RetryTaskFailMoreThresholdAlarmEvent(RetryTask retryTask) {
        super(retryTask);
        this.retryTask = retryTask;
    }
}
