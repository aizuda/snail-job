package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:09
 */
@Data
public class RetryTaskLogMessageResponseVO {

    private Long id;

    private String message;

    private LocalDateTime createDt;

}
