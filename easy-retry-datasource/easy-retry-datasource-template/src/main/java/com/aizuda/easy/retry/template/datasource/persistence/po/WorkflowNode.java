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
 * 工作流节点
 * </p>
 *
 * @author xiaowoniu
 * @since 2023-12-12
 */
@Getter
@Setter
@TableName("workflow_node")
public class WorkflowNode implements Serializable {

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
     * 节点名称
     */
    private String nodeName;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务信息id
     */
    private Long jobId;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 1、任务节点 2、条件节点 3、回调节点
     */
    private Integer nodeType;

    /**
     * 节点信息
     */
    private String nodeInfo;

    /**
     * 失败策略 1、跳过 2、阻塞
     */
    private Integer failStrategy;

    /**
     * 优先级
     */
    private Integer priorityLevel;

    /**
     * 工作流节点状态 0、关闭、1、开启
     */
    private Integer workflowNodeStatus;

    /**
     * 版本号
     */
    private Integer version;

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
    private Integer deleted;

    /**
     * 扩展字段
     */
    private String extAttrs;
}
