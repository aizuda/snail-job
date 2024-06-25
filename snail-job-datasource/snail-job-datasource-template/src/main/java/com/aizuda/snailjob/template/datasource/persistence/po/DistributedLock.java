package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 锁定表
 *
 * @author opensnail
 * @since 2023-07-20
 */
@Data
@TableName("sj_distributed_lock")
public class DistributedLock extends CreateUpdateDt {

    /**
     * 锁名称
     */
    @TableId(value = "name")
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

}
