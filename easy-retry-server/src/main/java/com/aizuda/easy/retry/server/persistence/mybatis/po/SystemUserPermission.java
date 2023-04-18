package com.aizuda.easy.retry.server.persistence.mybatis.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 系统用户权限表
 *
 * @author www.byteblogs.com
 * @since 2022-03-05
 */
@TableName("system_user_permission")
@Data
public class SystemUserPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String groupName;

    private Long systemUserId;

    private LocalDateTime createDt;

}
