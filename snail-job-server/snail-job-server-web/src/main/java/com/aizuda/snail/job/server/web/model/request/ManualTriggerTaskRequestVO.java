package com.aizuda.snail.job.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-09-11 22:00:26
 * @since 2.3.0
 */
@Data
public class ManualTriggerTaskRequestVO {

    @NotBlank(message = "groupName 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
    private String groupName;

    @NotEmpty(message = "uniqueIds 不能为空")
    private List<String> uniqueIds;

}
