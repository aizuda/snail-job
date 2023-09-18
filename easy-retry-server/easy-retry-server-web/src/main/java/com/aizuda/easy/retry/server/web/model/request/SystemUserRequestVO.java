package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-03-05
 * @since 2.0
 */
@Data
public class SystemUserRequestVO {

    @NotNull(groups = {PutMapping.class})
    private Long id;

    @NotBlank(message = "用户名不能为空", groups = PostMapping.class)
    private String username;

    @NotBlank(message = "密码不能为空", groups = PostMapping.class)
    private String password;

    @NotNull(groups = {PutMapping.class, PostMapping.class})
    private Integer role;

    private List<String> groupNameList;
}
