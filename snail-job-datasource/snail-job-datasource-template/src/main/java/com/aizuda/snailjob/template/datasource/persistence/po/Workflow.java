package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * * 工作流
 * </p>
 *
 * @author : xiaowoniu
 * @date : 2023-12-12
 * @since : 2.6.0
 */
@Data
@TableName("sj_workflow")
public class Workflow extends CreateUpdateDt {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 命名空间id
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 触发类型
     */
    private Integer triggerType;

    /**
     * 阻塞策略
     */
    private Integer blockStrategy;

    /**
     * 触发间隔
     */
    private String triggerInterval;

    /**
     * 执行超时时间
     */
    private Integer executorTimeout;

    /**
     * 工作流状态 0、关闭、1、开启
     */
    private Integer workflowStatus;

    /**
     * 任务执行时间
     */
    private Long nextTriggerAt;

    /**
     * 流程信息
     */
    private String flowInfo;

    /**
     * bucket
     */
    private Integer bucketIndex;

    /**
     * 描述
     */
    private String description;

    /**
     * 版本号
     */
    @TableField(value = "version", update = "%s+1")
    private Integer version;

    /**
     * 扩展字段
     */
    private String extAttrs;

    /**
     * 逻辑删除 1、删除
     */
    private Integer deleted;

}
