package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author: opensnail
 * @date : 2021-11-22 13:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupConfigQueryVO extends BaseQueryVO {

    private String groupName;
    private Integer groupStatus;

}
