package com.aizuda.snail.job.server.common.enums;

import com.aizuda.snail.job.server.common.exception.EasyRetryServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * job 触发器类型枚举
 *
 * @author: xiaowoniu
 * @date : 2023-12-06 17:21
 * @since : 2.5.0
 */
@Getter
@AllArgsConstructor
public enum JobTaskExecutorSceneEnum {
    AUTO_JOB(1, SyetemTaskTypeEnum.JOB),
    MANUAL_JOB(2,  SyetemTaskTypeEnum.JOB),
    AUTO_WORKFLOW(3, SyetemTaskTypeEnum.WORKFLOW),
    MANUAL_WORKFLOW(4, SyetemTaskTypeEnum.WORKFLOW),
    ;

    private final Integer type;
    private final SyetemTaskTypeEnum systemTaskType;

    /**
     * 根据给定的类型获取对应的触发器类型枚举
     *
     * @param type 触发器类型的整数值
     * @return 对应的触发器类型枚举
     * @throws EasyRetryServerException 当给定的类型不是有效的枚举类型时抛出异常
     */
    public static JobTaskExecutorSceneEnum get(Integer type) {
        for (JobTaskExecutorSceneEnum jobTaskExecutorSceneEnum : JobTaskExecutorSceneEnum.values()) {
            if(jobTaskExecutorSceneEnum.getType().equals(type)) {
                return jobTaskExecutorSceneEnum;
            }
        }

        throw new EasyRetryServerException("无效枚举类型.[{}]", type);
    }

}
