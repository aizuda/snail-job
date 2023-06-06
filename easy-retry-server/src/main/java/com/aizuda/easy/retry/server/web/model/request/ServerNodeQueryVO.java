package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;


/**
 * @author: www.byteblogs.com
 * @date : 2021-11-22 13:45
 */
@Data
public class ServerNodeQueryVO extends BaseQueryVO {

    private String groupName;

}
