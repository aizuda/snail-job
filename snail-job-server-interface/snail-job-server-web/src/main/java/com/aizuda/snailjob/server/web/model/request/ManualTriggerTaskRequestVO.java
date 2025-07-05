package com.aizuda.snailjob.server.web.model.request;

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

    @NotBlank(message = "groupName cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, underscores, and hyphens")
    private String groupName;

    @NotEmpty(message = "retryIds cannot be null")
    private List<Long> retryIds;

}
