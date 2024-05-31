package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author opensnail
 * @date 2024-04-17 21:26:22
 * @since sj_1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NotifyRecipientQueryVO extends BaseQueryVO {

    private Integer notifyType;

    private String recipientName;
}
