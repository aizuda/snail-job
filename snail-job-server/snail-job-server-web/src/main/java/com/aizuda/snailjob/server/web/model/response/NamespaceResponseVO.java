package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: xiaowoniu
 * @date : 2023-11-21 15:39
 * @since : 2.5.0
 */
@Data
public class NamespaceResponseVO {

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
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;

}
