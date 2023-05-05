package com.aizuda.easy.retry.server.persistence.mybatis.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 号段模式序号ID分配表
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2023-05-05
 */
@Getter
@Setter
@TableName("sequence_alloc")
public class SequenceAlloc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 组名称
     */
    private String groupName;

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
    private LocalDateTime updateDt;
}
