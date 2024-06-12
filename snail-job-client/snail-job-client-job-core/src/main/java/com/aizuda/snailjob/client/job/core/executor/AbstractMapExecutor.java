package com.aizuda.snailjob.client.job.core.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2024/06/12
 */
@Slf4j
public abstract class AbstractMapExecutor extends AbstractJobExecutor {

    @Override
    public void doMapExecute(List<?> taskList, String mapName) {

        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }
        System.out.println("TODO");
    }
}
