package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务通知配置
 *
 * @author opensnail
 * @since 2023-12-03
 */
@Data
@TableName("sj_job_notify_config")
@EqualsAndHashCode(callSuper=true)
public class JobNotifyConfig extends CreateUpdateDt {

    @TableId(value = "id")
    private Long id;

    private String namespaceId;

    private String groupName;

    private Long jobId;

    private Integer notifyStatus;

    private Integer notifyType;

    private String notifyAttribute;

    private Integer notifyThreshold;

    private Integer notifyScene;

    private Integer rateLimiterStatus;

    private Integer rateLimiterThreshold;

    private String description;

}
