package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 号段模式序号ID分配表
 *
 * @author opensnail
 * @since 2023-05-05
 */
@Data
@TableName("sj_sequence_alloc")
@Deprecated
public class SequenceAlloc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 命名空间ID
     */
    private String namespaceId;

    /**
     * 最大id
     */
    private Long maxId;

    /**
     * 步长
     */
    private Integer step;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDt;
}
