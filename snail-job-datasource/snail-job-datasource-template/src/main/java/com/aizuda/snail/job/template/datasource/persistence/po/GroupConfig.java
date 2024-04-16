package com.aizuda.snail.job.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组配置
 *
 * @author opensnail
 * @since 2023-01-14
 */
@Data
@TableName("sj_group_config")
public class GroupConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String namespaceId;

    private String groupName;

    private Integer groupStatus;

    private Integer groupPartition;

    private Integer idGeneratorMode;

    @TableField(value = "version", update = "%s+1")
    private Integer version;

    private Integer initScene;

    private Integer bucketIndex;

    private String token;

    private String description;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

}
