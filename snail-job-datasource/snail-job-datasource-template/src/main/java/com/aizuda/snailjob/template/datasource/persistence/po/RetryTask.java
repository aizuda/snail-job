package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode;

/**
 * 重试任务表
 * todo
 * @author opensnail
 * @since 2023-01-14
 */
@Data
@TableName("sj_retry_task")
@EqualsAndHashCode(callSuper=true)
public class RetryTask extends CreateUpdateDt {

    @TableId(value = "id")
    private Long id;

    private String namespaceId;

    private String groupName;

    private String sceneName;

    private Long retryId;

    private String extAttrs;

    private Integer taskStatus;

    private Integer taskType;

    private Integer operationReason;

    /**
     * 客户端ID
     */
    private String clientInfo;

}
