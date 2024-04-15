package com.aizuda.snail.job.template.datasource.persistence.dataobject;

import lombok.Data;

/**
 * @author zhengweilin
 * @version 2.6.0
 * @date 2023/11/28
 */
@Data
public class DashboardRetryResponseDO {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名
     */
    private String groupName;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 运行数量
     */
    private Integer runningNum;

    /**
     * 完成数量
     */
    private Integer finishNum;

    /**
     * 最大重试数量
     */
    private Integer maxCountNum;

    /**
     * 暂停
     */
    private Integer suspendNum;
}
