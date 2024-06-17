package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zhengweilin
 * @version 2.6.0
 * @date 2023/11/28
 */
@Data
@TableName("sj_retry_summary")
public class RetrySummary extends CreateUpdateDt {

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
     * 组名
     */
    private String groupName;

    /**
     * 调度时间
     */
    private LocalDateTime triggerAt;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 运行数量
     */
    private Integer runningNum;

    /**
     * 完成数量
     */
    private Integer finishNum;

    /**
     * 最大重试数量
     */
    private Integer maxCountNum;

    /**
     * 暂停
     */
    private Integer suspendNum;

}
