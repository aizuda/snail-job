package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 命名空间
 *
 * @author xiaowoniu
 * @since 2023-11-21
 */
@Data
@TableName("sj_namespace")
@EqualsAndHashCode(callSuper=true)
public class Namespace extends CreateUpdateDt {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 唯一id
     */
    private String uniqueId;

    /**
     * 逻辑删除 1、删除
     */
    private Integer deleted;

}
