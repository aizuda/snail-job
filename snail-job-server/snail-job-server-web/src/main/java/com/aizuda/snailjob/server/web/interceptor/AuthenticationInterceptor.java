package com.aizuda.snailjob.server.web.interceptor;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.exception.SnailJobAuthenticationException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NamespaceMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.SystemUserMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.SystemUserPermissionMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Namespace;
import com.aizuda.snailjob.template.datasource.persistence.po.SystemUser;
import com.aizuda.snailjob.template.datasource.persistence.po.SystemUserPermission;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * 系统登陆认证
 *
 * @author: byteblogs
 * @date:2023-04-26 12:52
 */
@Configuration
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    public static final String AUTHENTICATION = "SNAIL-JOB-AUTH";
    public static final String NAMESPACE_ID = "SNAIL-JOB-NAMESPACE-ID";
    private final SystemUserMapper systemUserMapper;
    private final NamespaceMapper namespaceMapper;
    private final SystemUserPermissionMapper systemUserPermissionMapper;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
        // 从 http 请求头中取出 token
        String token = httpServletRequest.getHeader(AUTHENTICATION);
        String namespaceId = httpServletRequest.getHeader(NAMESPACE_ID);

        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        // 检查是否有LoginRequired注释，没有有则跳过认证
        if (!method.isAnnotationPresent(LoginRequired.class)) {
            return true;
        }

        LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
        if (loginRequired.required()) {
            // 执行认证
            if (token == null) {
                throw new SnailJobAuthenticationException("登陆过期，请重新登陆");
            }

            if (StrUtil.isBlank(namespaceId)) {
                throw new SnailJobAuthenticationException("{} 命名空间不存在", namespaceId);
            }

            // 获取 token 中的 user id
            SystemUser systemUser;
            try {
                systemUser = JsonUtil.parseObject(JWT.decode(token).getAudience().get(0), SystemUser.class);
            } catch (JWTDecodeException j) {
                throw new SnailJobAuthenticationException("登陆过期，请重新登陆");
            }

            systemUser = systemUserMapper.selectById(systemUser.getId());
            if (Objects.isNull(systemUser)) {
                throw new SnailJobAuthenticationException("用户不存在");
            }

            Long count = namespaceMapper.selectCount(
                    new LambdaQueryWrapper<Namespace>().eq(Namespace::getUniqueId, namespaceId));
            Assert.isTrue(count > 0, () -> new SnailJobServerException("[{}] 命名空间不存在", namespaceId));
            UserSessionVO userSessionVO = new UserSessionVO();
            userSessionVO.setId(systemUser.getId());
            userSessionVO.setUsername(systemUser.getUsername());
            userSessionVO.setRole(systemUser.getRole());
            userSessionVO.setNamespaceId(namespaceId);

            // 普通用户才获取权限
            if (userSessionVO.isUser()) {
                List<SystemUserPermission> systemUserPermissions = systemUserPermissionMapper.selectList(
                        new LambdaQueryWrapper<SystemUserPermission>()
                                .select(SystemUserPermission::getGroupName)
                                .eq(SystemUserPermission::getSystemUserId, systemUser.getId())
                                .eq(SystemUserPermission::getNamespaceId, namespaceId)
                );
                userSessionVO.setGroupNames(StreamUtils.toList(systemUserPermissions, SystemUserPermission::getGroupName));
            }

            httpServletRequest.setAttribute("currentUser", userSessionVO);

            // 验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(systemUser.getPassword())).build();
            try {
                jwtVerifier.verify(token);
            } catch (JWTVerificationException e) {
                throw new SnailJobAuthenticationException("登陆过期，请重新登陆");
            }

            RoleEnum role = loginRequired.role();
            if (role == RoleEnum.USER) {
                return true;
            }

            if (role == RoleEnum.ADMIN) {
                if (role != RoleEnum.getEnumTypeMap().get(systemUser.getRole())) {
                    throw new SnailJobAuthenticationException("不具备访问权限");
                }
            }

            return true;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
    }
}
