package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


/**
 * @author: www.byteblogs.com
 * @date : 2021-11-22 13:45
 */
@Data
public class GroupConfigQueryVO extends BaseQueryVO {

    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
    private String groupName;

    @NotNull(message = "组状态不能为空")
    private Integer groupStatus;

}
