package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户权限表
 *
 * @author opensnail
 * @since 2022-03-05
 */
@Data
@TableName("sj_system_user_permission")
@EqualsAndHashCode(callSuper=true)
public class SystemUserPermission extends CreateDt {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String groupName;

    private String namespaceId;

    private Long systemUserId;

}
