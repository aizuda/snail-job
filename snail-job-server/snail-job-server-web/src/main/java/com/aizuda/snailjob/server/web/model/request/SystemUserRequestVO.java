package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

/**
 * @author opensnail
 * @date 2022-03-05
 * @since 2.0
 */
@Data
public class SystemUserRequestVO {

    @NotNull(groups = {PutMapping.class})
    private Long id;

    @NotBlank(message = "Username cannot be empty", groups = PostMapping.class)
    private String username;

    @NotBlank(message = "Password cannot be empty", groups = PostMapping.class)
    private String password;

    @NotNull(groups = {PutMapping.class, PostMapping.class})
    private Integer role;

    private List<UserPermissionRequestVO> permissions;
}
