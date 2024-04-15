package com.aizuda.snail.job.server.web.model.request;

import com.aizuda.snail.job.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: xiaowoniu
 * @date : 2023-11-21 15:21
 * @since : 2.5.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NamespaceQueryVO extends BaseQueryVO {

    private String keyword;

}
