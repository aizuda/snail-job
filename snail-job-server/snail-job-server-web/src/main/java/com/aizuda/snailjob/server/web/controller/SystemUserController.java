package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.LoginUser;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.SystemUserQueryVO;
import com.aizuda.snailjob.server.web.model.request.SystemUserRequestVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.PermissionsResponseVO;
import com.aizuda.snailjob.server.web.model.response.SystemUserResponseVO;
import com.aizuda.snailjob.server.web.service.SystemUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户表 前端控制器
 *
 * @author opensnail
 * @since 2022-03-05
 */
@RestController
public class SystemUserController {

    private final Long SUPER_ADMIN_ID = 1L;

    @Autowired
    private SystemUserService systemUserService;

    @PostMapping("/auth/login")
    public SystemUserResponseVO login(@RequestBody SystemUserRequestVO requestVO) {
        return systemUserService.login(requestVO);
    }

    @LoginRequired
    @GetMapping("/user/info")
    public SystemUserResponseVO getUserInfo(@LoginUser UserSessionVO systemUser) {
        return systemUserService.getUserInfo(systemUser);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @PostMapping("/user")
    public void addUser(@RequestBody @Valid SystemUserRequestVO requestVO) {
        systemUserService.addUser(requestVO);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @GetMapping("/user/page/list")
    public PageResult<List<SystemUserResponseVO>> getSystemUserPageList(SystemUserQueryVO systemUserQueryVO) {
        return systemUserService.getSystemUserPageList(systemUserQueryVO);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @PutMapping("/user")
    public void update(@RequestBody @Valid SystemUserRequestVO requestVO) {
        if (requestVO.getId() == SUPER_ADMIN_ID && requestVO.getRole().equals(RoleEnum.USER.getRoleId())) {
            throw new SnailJobServerException("不允许修改超级管理员角色");
        }
        systemUserService.update(requestVO);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @GetMapping("/user/username/user-info")
    public SystemUserResponseVO getSystemUserByUserName(@RequestParam("username") String username) {
        return systemUserService.getSystemUserByUserName(username);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @GetMapping("/user-permissions/{id}")
    public List<PermissionsResponseVO> getSystemUserPermissionByUserName(@PathVariable("id") Long id) {
        return systemUserService.getSystemUserPermissionByUserName(id);
    }

    @LoginRequired
    @DeleteMapping("/user/{id}")
    public boolean delUser(@PathVariable("id") Long id) {
        if (id == SUPER_ADMIN_ID) {
            throw new SnailJobServerException("不允许删除超级管理员");
        }
        return systemUserService.delUser(id);
    }
}
