package com.aizuda.easy.retry.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户
 *
 * @author www.byteblogs.com
 * @since 2022-03-05
 */
@TableName("system_user") // NOTE: system_user表是SQL Server系统函数, 这里需要修改为 system_user_, [system_user]不支持子查询
@Data
public class SystemUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private Integer role;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

}
