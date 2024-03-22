package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:09
 */
@Data
public class RetryTaskLogMessageResponseVO {

    private Long id;

    /**
     * 客户端信息
     */
    private String clientInfo;

    private LocalDateTime createDt;

    private Long nextStartId;

    private List message;

    private boolean isFinished;

    private Integer fromIndex;

}
