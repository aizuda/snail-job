package com.aizuda.easy.retry.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 工作流
 * </p>
 *
 * @author : xiaowoniu
 * @date : 2023-12-12
 * @since : 2.6.0
 */
@Getter
@Setter
@TableName("workflow")
public class Workflow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 命名空间id
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 工作流状态 0、关闭、1、开启
     */
    private Byte workflowStatus;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    /**
     * 流程信息
     */
    private String flowInfo;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;

    /**
     * 逻辑删除 1、删除
     */
    private Byte deleted;

    /**
     * 扩展字段
     */
    private String extAttrs;
}
