package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 重试任务日志
 *
 * @author opensnail
 * @since 2023-01-14
 */
@Data
@TableName("sj_retry_task_log")
public class RetryTaskLog extends CreateUpdateDt {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String namespaceId;

    private String uniqueId;

    private String groupName;

    private String sceneName;

    private String idempotentId;

    private String bizNo;

    private String executorName;

    private String argsStr;

    private String extAttrs;

    private Integer retryStatus;

    private Integer taskType;

}
