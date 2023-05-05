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
public class GroupConfigQueryVO extends BaseQueryVO {


    @NotBlank(message = "组名称不能为空")
    private String groupName;

    @NotNull(message = "组状态不能为空")
    private Integer groupStatus;

}
