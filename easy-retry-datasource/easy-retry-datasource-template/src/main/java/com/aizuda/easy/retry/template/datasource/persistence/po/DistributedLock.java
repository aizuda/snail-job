package com.aizuda.easy.retry.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 锁定表
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2023-07-20
 */
@Getter
@Setter
@TableName("distributed_lock")
public class DistributedLock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 锁名称
     */
    private String name;

    /**
     * 锁定时长
     */
    private LocalDateTime lockUntil;

    /**
     * 锁定时间
     */
    private LocalDateTime lockedAt;

    /**
     * 锁定者
     */
    private String lockedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;


}
