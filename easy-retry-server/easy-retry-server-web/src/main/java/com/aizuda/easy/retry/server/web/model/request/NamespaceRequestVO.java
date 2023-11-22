package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author: xiaowoniu
 * @date : 2023-11-21 15:15
 * @since : 2.5.0
 */
@Data
public class NamespaceRequestVO {

    private Long id;

    /**
     * 命名空间唯一标识
     */
    String uniqueId;

    /**
     * 名称
     */
    @NotBlank(message= "name 不能为空")
    private String name;

}
