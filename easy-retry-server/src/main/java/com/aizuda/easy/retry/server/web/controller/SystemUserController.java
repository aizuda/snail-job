package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.template.datasource.persistence.po.SystemUser;
import com.aizuda.easy.retry.server.service.SystemUserService;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.SystemUserQueryVO;
import com.aizuda.easy.retry.server.web.model.request.SystemUserRequestVO;
import com.aizuda.easy.retry.server.web.annotation.LoginUser;
import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.annotation.RoleEnum;
import com.aizuda.easy.retry.server.web.model.response.SystemUserResponseVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户表 前端控制器
 *
 * @author www.byteblogs.com
 * @since 2022-03-05
 */
@RestController
public class SystemUserController {

    @Autowired
    private SystemUserService systemUserService;

    @PostMapping("/auth/login")
    public SystemUserResponseVO login(@RequestBody SystemUserRequestVO requestVO) {
        return systemUserService.login(requestVO);
    }

    @LoginRequired
    @GetMapping("/user/info")
    public SystemUserResponseVO getUserInfo(@LoginUser SystemUser systemUser) {
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
        systemUserService.update(requestVO);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @GetMapping("/user/username/user-info")
    public SystemUserResponseVO getSystemUserByUserName(@RequestParam("username") String username) {
        return systemUserService.getSystemUserByUserName(username);
    }

    @LoginRequired
    @DeleteMapping("/user/{id}")
    public boolean delUser(@PathVariable("id") Long id) {
        return systemUserService.delUser(id);
    }
}
