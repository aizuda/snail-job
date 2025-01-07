package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流节点
 *
 * @author xiaowoniu
 * @since 2023-12-12
 */
@Data
@TableName("sj_workflow_node")
@EqualsAndHashCode(callSuper=true)
public class WorkflowNode extends CreateUpdateDt {

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
     * 扩展字段
     */
    private String extAttrs;

    /**
     * 逻辑删除 1、删除
     */
    private Integer deleted;

}
