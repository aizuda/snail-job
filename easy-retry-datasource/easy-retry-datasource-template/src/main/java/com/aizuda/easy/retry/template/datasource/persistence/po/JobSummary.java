package com.aizuda.easy.retry.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zhengweilin
 * @version 2.6.0
 * @date 2023/11/22
 */
@Getter
@Setter
@TableName("job_summary")
public class JobSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名
     */
    private String groupName;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * '任务信息id'
     */
    private Long businessId;

    /**
     * '统计时间'
     */
    private LocalDateTime triggerAt;

    /**
     * 执行成功-日志数量
     */
    private Integer successNum;

    /**
     * 执行失败-日志数量
     */
    private Integer failNum;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 执行失败-日志数量
     */
    private Integer stopNum;

    /**
     * 失败原因
     */
    private String stopReason;

    /**
     * 执行失败-日志数量
     */
    private Integer cancelNum;

    /**
     * 失败原因
     */
    private String cancelReason;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;

    /**
     * 任务类型 3、JOB任务 4、WORKFLOW任务
     */
    private Integer systemTaskType;
}
