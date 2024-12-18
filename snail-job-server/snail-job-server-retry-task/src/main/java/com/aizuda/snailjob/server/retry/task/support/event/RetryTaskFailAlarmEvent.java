package com.aizuda.snailjob.server.retry.task.support.event;

import com.aizuda.snailjob.template.datasource.persistence.dataobject.RetryTaskFailAlarmEventDO;
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

    private RetryTaskFailAlarmEventDO retryTaskFailAlarmEventDO;

    public RetryTaskFailAlarmEvent(RetryTaskFailAlarmEventDO retryTaskFailAlarmEventDO) {
        super(retryTaskFailAlarmEventDO);
        this.retryTaskFailAlarmEventDO = retryTaskFailAlarmEventDO;
    }
}
