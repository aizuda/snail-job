package com.aizuda.snailjob.server.retry.task.support.event;

import com.aizuda.snailjob.server.retry.task.dto.RetryTaskFailAlarmEventDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 重试任务失败事件
 *
 * @author: zhengweilin
 * @date : 2024-12-13 16:57
 * @since 1.3.0
 */
@Getter
public class RetryTaskFailAlarmEvent extends ApplicationEvent {

    private final RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDTO;

    public RetryTaskFailAlarmEvent(RetryTaskFailAlarmEventDTO retryTaskFailAlarmEventDTO) {
        super(retryTaskFailAlarmEventDTO);
        this.retryTaskFailAlarmEventDTO = retryTaskFailAlarmEventDTO;
    }
}
