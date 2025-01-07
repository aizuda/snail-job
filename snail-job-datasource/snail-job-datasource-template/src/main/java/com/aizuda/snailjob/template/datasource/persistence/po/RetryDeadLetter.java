package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 死信队列
 */
@Data
@TableName("sj_retry_dead_letter")
@EqualsAndHashCode(callSuper=true)
public class RetryDeadLetter extends CreateDt {

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

    private Integer taskType;

}
