package com.aizuda.snailjob.server.retry.task.support.event;

import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 重试任务失败进入死信队列事件
 *
 * @author: zuoJunLin
 * @date : 2023-11-20 21:40
 * @since 2.5.0
 */
public class RetryTaskFailDeadLetterAlarmEvent extends ApplicationEvent {
    private List<RetryDeadLetter> retryDeadLetters;

    public RetryTaskFailDeadLetterAlarmEvent(List<RetryDeadLetter> retryDeadLetters) {
        super(retryDeadLetters);
        this.retryDeadLetters = retryDeadLetters;
    }

    public List<RetryDeadLetter> getRetryDeadLetters() {
        return retryDeadLetters;
    }
}
