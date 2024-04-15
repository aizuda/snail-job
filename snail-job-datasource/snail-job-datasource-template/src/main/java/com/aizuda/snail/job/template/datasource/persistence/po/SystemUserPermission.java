package com.aizuda.snail.job.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户权限表
 *
 * @author opensnail
 * @since 2022-03-05
 */
@TableName("system_user_permission")
@Data
public class SystemUserPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String groupName;

    private String namespaceId;

    private Long systemUserId;

    private LocalDateTime createDt;

}
