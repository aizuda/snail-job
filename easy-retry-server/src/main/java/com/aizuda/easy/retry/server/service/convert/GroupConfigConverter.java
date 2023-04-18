package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.common.core.covert.AbstractConverter;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GroupConfigVO装换为GroupConfig
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-26 13:55
 */
public class GroupConfigConverter extends AbstractConverter<GroupConfigRequestVO, GroupConfig> {

    @Override
    public GroupConfig convert(GroupConfigRequestVO groupConfigRequestVO) {

        GroupConfig groupConfig = convert(groupConfigRequestVO, GroupConfig.class);
        groupConfig.setUpdateDt(LocalDateTime.now());

        return groupConfig;
    }

    @Override
    public List<GroupConfig> batchConvert(List<GroupConfigRequestVO> groupConfigRequestVOS) {
        return null;
    }
}
