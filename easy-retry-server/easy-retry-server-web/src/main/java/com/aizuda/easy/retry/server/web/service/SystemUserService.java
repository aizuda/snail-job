package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.SystemUserQueryVO;
import com.aizuda.easy.retry.server.web.model.request.SystemUserRequestVO;
import com.aizuda.easy.retry.server.web.model.response.SystemUserResponseVO;

import java.util.List;

/**
 * <p>
 * 系统用户表 服务类
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2022-03-05
 */
public interface SystemUserService {

    SystemUserResponseVO login(SystemUserRequestVO requestVO);

    SystemUserResponseVO getUserInfo(UserSessionVO systemUser);

    void addUser(SystemUserRequestVO requestVO);

    void update(SystemUserRequestVO requestVO);

    PageResult<List<SystemUserResponseVO>> getSystemUserPageList(SystemUserQueryVO queryVO);

    SystemUserResponseVO getSystemUserByUserName(String username);

    boolean delUser(Long id);
}
