package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
    @NotBlank(message = "name cannot be empty")
    private String name;

}
