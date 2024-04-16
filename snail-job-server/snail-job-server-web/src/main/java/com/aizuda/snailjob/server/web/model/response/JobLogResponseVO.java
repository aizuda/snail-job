package com.aizuda.snailjob.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: opensnail
 * @date : 2023-10-12 11:22
 * @since : 2.4.0
 */
@Data
public class JobLogResponseVO {

    private Long id;

    private Long nextStartId;

    private List message;

    private boolean isFinished;

    private Integer fromIndex;

}
