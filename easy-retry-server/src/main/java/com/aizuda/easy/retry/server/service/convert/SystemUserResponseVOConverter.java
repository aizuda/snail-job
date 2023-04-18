package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.SystemUser;
import com.aizuda.easy.retry.common.core.covert.AbstractConverter;
import com.aizuda.easy.retry.server.web.model.response.SystemUserResponseVO;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-03-05
 * @since 2.0
 */
public class SystemUserResponseVOConverter extends AbstractConverter<SystemUser, SystemUserResponseVO> {

    @Override
    public SystemUserResponseVO convert(SystemUser systemUser) {
        return convert(systemUser, SystemUserResponseVO.class);
    }

    @Override
    public List<SystemUserResponseVO> batchConvert(List<SystemUser> systemUsers) {
        return batchConvert(systemUsers, SystemUserResponseVO.class);
    }
}
