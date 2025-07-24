package com.aizuda.snailjob.server.common.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2023-10-12 11:22
 * @since : 2.4.0
 */
@Data
@Deprecated
public class JobLogResponseVO {

    private Long id;

    private Long nextStartId;

    private List message;

    private boolean isFinished;

    private Integer fromIndex;

}
