package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务统计
 *
 * @author zhengweilin
 * @version 2.6.0
 * @date 2023/11/22
 */
@Data
@TableName("sj_job_summary")
@EqualsAndHashCode(callSuper=true)
public class JobSummary extends CreateUpdateDt {

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
    @TableId(value = "id")
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
     * 任务类型 3、JOB任务 4、WORKFLOW任务
     */
    private Integer systemTaskType;

}
