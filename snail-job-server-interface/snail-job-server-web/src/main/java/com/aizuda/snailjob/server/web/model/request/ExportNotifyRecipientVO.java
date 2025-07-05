package com.aizuda.snailjob.server.web.model.request;

import lombok.Data;

import java.util.Set;

/**
 * @author: opensnail
 * @date : 2024-05-31
 * @since : sj_1.0.0
 */
@Data
public class ExportNotifyRecipientVO {

    private Set<Long> notifyRecipientIds;

    private Integer notifyType;

    private String recipientName;
}
