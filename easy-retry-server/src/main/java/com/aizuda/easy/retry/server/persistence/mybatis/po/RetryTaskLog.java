package com.aizuda.easy.retry.server.persistence.mybatis.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RetryTaskLog implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    private LocalDateTime createDt;

    private static final long serialVersionUID = 1L;

}
