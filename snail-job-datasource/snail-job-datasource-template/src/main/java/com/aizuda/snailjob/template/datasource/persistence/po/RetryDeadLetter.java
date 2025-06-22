package com.aizuda.snailjob.template.datasource.persistence.po;

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

    @TableId(value = "id")
    private Long id;

    private String namespaceId;

    private String groupName;

    private Long groupId;

    private String sceneName;

    private Long sceneId;

    private String idempotentId;

    private String bizNo;

    private String executorName;

    private String serializerName;

    private String argsStr;

    private String extAttrs;
}
