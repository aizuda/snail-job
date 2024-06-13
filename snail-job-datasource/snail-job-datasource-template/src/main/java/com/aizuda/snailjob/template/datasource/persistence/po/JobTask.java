package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 任务实例
 *
 * @author opensnail
 * @since 2023-09-24
 */
@Data
@TableName("sj_job_task")
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
     * 参数类型 text/json
     */
    private Integer argsType;

    /**
     * 扩展字段
     */
    private String extAttrs;

}
