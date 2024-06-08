package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * @author dhb52
 * @date 2024-06-09
 * @since 1.0.0
 */
@Data
public class SystemUpdateUserPasswordRequestVO {

    @NotBlank(message = "原密码不能为空", groups = PutMapping.class)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空", groups = PutMapping.class)
    private String newPassword;

}
