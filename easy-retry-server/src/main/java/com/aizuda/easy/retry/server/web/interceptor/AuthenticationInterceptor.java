package com.aizuda.easy.retry.server.web.interceptor;

import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.SystemUserMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.SystemUser;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.annotation.RoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 系统登陆认证
 *
 * @author: byteblogs
 * @date:2023-04-26 12:52
 */
@Configuration
public class AuthenticationInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATION = "EASY-RETRY-AUTH";

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
        // 从 http 请求头中取出 token
        String token = httpServletRequest.getHeader(AUTHENTICATION);

        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();
        // 检查是否有LoginRequired注释，没有有则跳过认证
        if (!method.isAnnotationPresent(LoginRequired.class)) {
            return true;
        }

        LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
        if (loginRequired.required()) {
            // 执行认证
            if (token == null) {
                throw new EasyRetryServerException("登陆过期，请重新登陆");
            }

            // 获取 token 中的 user id
            SystemUser systemUser;
            try {
                systemUser = JsonUtil.parseObject(JWT.decode(token).getAudience().get(0), SystemUser.class);
            } catch (JWTDecodeException j) {
                throw new EasyRetryServerException("登陆过期，请重新登陆");
            }

            systemUser = systemUserMapper.selectOne(new LambdaQueryWrapper<SystemUser>().eq(SystemUser::getUsername, systemUser.getUsername()));
            if (Objects.isNull(systemUser)) {
                throw new EasyRetryServerException("{} 用户不存在", systemUser.getUsername());
            }

            httpServletRequest.setAttribute("currentUser", systemUser);

            // 验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(systemUser.getPassword())).build();
            try {
                jwtVerifier.verify(token);
            } catch (JWTVerificationException e) {
                throw new EasyRetryServerException("登陆过期，请重新登陆");
            }

            RoleEnum role = loginRequired.role();
            if (role == RoleEnum.USER) {
                return true;
            }

            if (role == RoleEnum.ADMIN) {
                if (role != RoleEnum.getEnumTypeMap().get(systemUser.getRole())) {
                    throw new EasyRetryServerException("不具备访问权限");
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
