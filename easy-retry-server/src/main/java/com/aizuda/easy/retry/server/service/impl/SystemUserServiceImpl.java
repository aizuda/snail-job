package com.aizuda.easy.retry.server.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.SystemUserMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.SystemUserPermissionMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.SystemUser;
import com.aizuda.easy.retry.template.datasource.persistence.po.SystemUserPermission;
import com.aizuda.easy.retry.server.service.SystemUserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.service.convert.SystemUserResponseVOConverter;
import com.aizuda.easy.retry.server.web.annotation.RoleEnum;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.SystemUserQueryVO;
import com.aizuda.easy.retry.server.web.model.request.SystemUserRequestVO;
import com.aizuda.easy.retry.server.web.model.response.SystemUserResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2022-03-05
 */
@Service
public class SystemUserServiceImpl implements SystemUserService {

    public static final long EXPIRE_TIME = 3600 * 1000;

    @Autowired
    private SystemUserMapper systemUserMapper;
    @Autowired
    private SystemUserPermissionMapper systemUserPermissionMapper;

    @Override
    public SystemUserResponseVO login(SystemUserRequestVO requestVO) {

        SystemUser systemUser = systemUserMapper.selectOne(new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getUsername, requestVO.getUsername()));
        if (Objects.isNull(systemUser)) {
            throw new EasyRetryServerException("用户名或密码错误");
        }

        if (!SecureUtil.sha256(requestVO.getPassword()).equals(systemUser.getPassword())) {
            throw new EasyRetryServerException("用户名或密码错误");
        }

        String token = getToken(systemUser);

        SystemUserResponseVO systemUserResponseVO = SystemUserResponseVOConverter.INSTANCE.convert(systemUser);
        systemUserResponseVO.setToken(token);

        if (RoleEnum.ADMIN.getRoleId().equals(systemUser.getRole())) {
            return systemUserResponseVO;
        }

        List<SystemUserPermission> systemUserPermissions = systemUserPermissionMapper.selectList(new LambdaQueryWrapper<SystemUserPermission>()
                .eq(SystemUserPermission::getSystemUserId, systemUser.getId()));
        systemUserResponseVO.setGroupNameList(systemUserPermissions.stream()
                .map(SystemUserPermission::getGroupName).collect(Collectors.toList()));

        return systemUserResponseVO;
    }

    @Override
    public SystemUserResponseVO getUserInfo(SystemUser systemUser) {
        SystemUserResponseVO systemUserResponseVO = SystemUserResponseVOConverter.INSTANCE.convert(systemUser);

        if (RoleEnum.ADMIN.getRoleId().equals(systemUser.getRole())) {
            return systemUserResponseVO;
        }

        List<SystemUserPermission> systemUserPermissions = systemUserPermissionMapper.selectList(new LambdaQueryWrapper<SystemUserPermission>()
                .eq(SystemUserPermission::getSystemUserId, systemUser.getId()));
        systemUserResponseVO.setGroupNameList(systemUserPermissions.stream()
                .map(SystemUserPermission::getGroupName).collect(Collectors.toList()));

        return systemUserResponseVO;
    }

    @Override
    @Transactional
    public void addUser(SystemUserRequestVO requestVO) {
        long count = systemUserMapper.selectCount(new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getUsername, requestVO.getUsername()));
        if (count > 0) {
            throw new EasyRetryServerException("该用户已存在");
        }

        SystemUser systemUser = new SystemUser();
        systemUser.setUsername(requestVO.getUsername());
        systemUser.setPassword(SecureUtil.sha256(requestVO.getPassword()));
        systemUser.setRole(requestVO.getRole());

        Assert.isTrue(1 == systemUserMapper.insert(systemUser), () ->  new EasyRetryServerException("新增用户失败"));

        // 只添加为普通用户添加权限
        List<String> groupNameList = requestVO.getGroupNameList();
        if (CollectionUtils.isEmpty(groupNameList) || RoleEnum.ADMIN.getRoleId().equals(requestVO.getRole())) {
            return;
        }

        for (String groupName : groupNameList) {
            SystemUserPermission systemUserPermission = new SystemUserPermission();
            systemUserPermission.setSystemUserId(systemUser.getId());
            systemUserPermission.setGroupName(groupName);
            Assert.isTrue(1 == systemUserPermissionMapper.insert(systemUserPermission), () ->  new EasyRetryServerException("新增用户权限失败"));
        }

    }

    @Override
    @Transactional
    public void update(SystemUserRequestVO requestVO) {
        SystemUser systemUser = systemUserMapper.selectOne(new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getId, requestVO.getId()));
        if (Objects.isNull(systemUser)) {
            throw new EasyRetryServerException("该用户不存在");
        }

        if (!systemUser.getUsername().equals(requestVO.getUsername())) {
            long count = systemUserMapper.selectCount(new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getUsername, requestVO.getUsername()));
            if (count > 0) {
                throw new EasyRetryServerException("该用户已存在");
            }
        }

        systemUser.setUsername(requestVO.getUsername());
        if (StrUtil.isNotBlank(requestVO.getPassword())) {
            systemUser.setPassword(SecureUtil.sha256(requestVO.getPassword()));
        }

        systemUser.setRole(requestVO.getRole());

        Assert.isTrue(1 == systemUserMapper.updateById(systemUser), () ->  new EasyRetryServerException("更新用户失败"));

        // 只添加为普通用户添加权限
        List<String> groupNameList = requestVO.getGroupNameList();
        if (CollectionUtils.isEmpty(groupNameList) || RoleEnum.ADMIN.getRoleId().equals(requestVO.getRole())) {
            return;
        }

        systemUserPermissionMapper.delete(new LambdaQueryWrapper<SystemUserPermission>()
                .eq(SystemUserPermission::getSystemUserId, systemUser.getId()));

        for (String groupName : groupNameList) {
            SystemUserPermission systemUserPermission = new SystemUserPermission();
            systemUserPermission.setSystemUserId(systemUser.getId());
            systemUserPermission.setGroupName(groupName);
            Assert.isTrue(1 == systemUserPermissionMapper.insert(systemUserPermission), () ->  new EasyRetryServerException("更新用户权限失败"));
        }
    }

    @Override
    public PageResult<List<SystemUserResponseVO>> getSystemUserPageList(SystemUserQueryVO queryVO) {
        PageDTO<SystemUser> userPageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<SystemUser> systemUserLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(queryVO.getUsername())) {
            systemUserLambdaQueryWrapper.like(SystemUser::getUsername, "%" + queryVO.getUsername() + "%");
        }

        userPageDTO = systemUserMapper.selectPage(userPageDTO, systemUserLambdaQueryWrapper.orderByDesc(SystemUser::getId));

        List<SystemUserResponseVO> userResponseVOList = SystemUserResponseVOConverter.INSTANCE.batchConvert(userPageDTO.getRecords());

        userResponseVOList.stream()
                .filter(systemUserResponseVO -> systemUserResponseVO.getRole().equals(RoleEnum.USER.getRoleId()))
                .forEach(systemUserResponseVO -> {
                    List<SystemUserPermission> systemUserPermissionList = systemUserPermissionMapper.selectList(
                            new LambdaQueryWrapper<SystemUserPermission>()
                                    .select(SystemUserPermission::getGroupName)
                                    .eq(SystemUserPermission::getSystemUserId, systemUserResponseVO.getId()));

                    systemUserResponseVO.setGroupNameList(systemUserPermissionList.stream()
                            .map(SystemUserPermission::getGroupName).collect(Collectors.toList()));
                });

        return new PageResult<>(userPageDTO, userResponseVOList);
    }

    @Override
    public SystemUserResponseVO getSystemUserByUserName(String username) {
        SystemUser systemUser = systemUserMapper.selectOne(new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getUsername, username));
        if (Objects.isNull(systemUser)) {
            throw new EasyRetryServerException("用户不存在");
        }

        return getUserInfo(systemUser);
    }

    @Override
    public boolean delUser(final Long id) {
        return 1 == systemUserMapper.deleteById(id);
    }

    /**
     * 生成Token
     */
    private String getToken(SystemUser systemUser) {
        String sign = systemUser.getPassword();
        return JWT.create().withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .withAudience(JsonUtil.toJsonString(SystemUserResponseVOConverter.INSTANCE.convert(systemUser)))
                .sign(Algorithm.HMAC256(sign));
    }
}
