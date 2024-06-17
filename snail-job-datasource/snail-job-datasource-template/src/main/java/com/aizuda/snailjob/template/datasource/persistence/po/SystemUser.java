package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统用户
 *
 * @author opensnail
 * @since 2022-03-05
 */
@Data
@TableName("sj_system_user")
public class SystemUser extends CreateUpdateDt {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private Integer role;

}
