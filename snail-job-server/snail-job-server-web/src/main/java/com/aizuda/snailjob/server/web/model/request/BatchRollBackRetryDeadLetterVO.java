package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量回滚死信表数据
 *
 * @author: opensnail
 * @date : 2023-04-30 22:30
 */
@Data
public class BatchRollBackRetryDeadLetterVO {

    /**
     * 重试表id
     */
    @NotEmpty(message = "At least one item must be selected")
    private List<Long> ids;
}
