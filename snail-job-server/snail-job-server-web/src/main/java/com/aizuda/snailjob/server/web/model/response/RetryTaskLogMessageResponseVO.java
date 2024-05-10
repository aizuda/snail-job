package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:09
 */
@Data
@Accessors(chain = true)
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
