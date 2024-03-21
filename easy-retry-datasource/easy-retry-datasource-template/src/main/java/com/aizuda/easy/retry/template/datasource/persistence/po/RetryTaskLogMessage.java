package com.aizuda.easy.retry.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 重试日志异常信息记录表
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2023-06-16
 */
@Getter
@Setter
@TableName("retry_task_log_message")
public class RetryTaskLogMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 同组下id唯一
     */
    private String uniqueId;

    /**
     * 客户端信息
     */
    private String clientInfo;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 异常信息
     */
    private String message;

    /**
     * 日志数量
     */
    private Integer logNum;


    /**
     * 真实上报时间
     */
    private Long realTime;
}
