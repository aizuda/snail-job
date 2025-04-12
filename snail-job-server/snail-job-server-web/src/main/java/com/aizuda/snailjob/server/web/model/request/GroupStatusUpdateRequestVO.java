package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author opensnail
 * @date 2024-06-01 11:37:45
 * @since sj_1.0.0
 */
@Data
public class GroupStatusUpdateRequestVO {

    @NotBlank(message = "Group name cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, and underscores")
    private String groupName;

    @NotNull(message = "Group status cannot be null")
    private Integer groupStatus;
}
