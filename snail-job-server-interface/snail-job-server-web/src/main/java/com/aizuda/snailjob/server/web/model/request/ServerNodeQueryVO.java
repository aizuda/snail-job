package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.service.dto.base.BaseQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author: opensnail
 * @date : 2021-11-22 13:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServerNodeQueryVO extends BaseQueryDTO {

    private String groupName;

}
