package com.aizuda.snailjob.template.datasource.persistence.po;

import com.aizuda.snailjob.common.core.enums.JobArgsTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.MapReduceStageEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务实例
 *
 * @author opensnail
 * @since 2023-09-24
 */
@Data
@TableName("sj_job_task")
@EqualsAndHashCode(callSuper=true)
public class JobTask extends CreateUpdateDt {

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
     * 任务名称
     */
    private String taskName;

    /**
     * 任务信息id
     */
    private Long jobId;

    /**
     * 调度任务id
     */
    private Long taskBatchId;

    /**
     * 父执行器id
     */
    private Long parentId;

    /**
     * 执行状态
     * {@link JobTaskStatusEnum}
     */
    private Integer taskStatus;

    /**
     * 重试次数
     */
    @TableField(value = "retry_count", update = "%s+1")
    private Integer retryCount;

    /**
     * 执行结果
     */
    private String resultMessage;

    /**
     * 客户端ID
     */
    private String clientInfo;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text、json
     * {@link JobArgsTypeEnum}
     */
    private Integer argsType;

    /**
     * 叶子节点(0:非叶子节点 1:叶子节点)
     * {@link StatusEnum}
     */
    private Integer leaf;

    /**
     * 动态分片使用
     * 1:map 2:reduce 3:mergeReduce
     * {@link MapReduceStageEnum}
     */
    private Integer mrStage;

    /**
     * 冗余工作流上下文
     * 注: 采用空间换时间的方式冗余部分上下文，减少更新并发
     */
    private String wfContext;

    /**
     * 扩展字段
     */
    private String extAttrs;

}
