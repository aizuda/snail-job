package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.common.core.covert.AbstractConverter;
import com.aizuda.easy.retry.server.web.model.response.GroupConfigResponseVO;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
public class GroupConfigResponseVOConverter  extends AbstractConverter<GroupConfig, GroupConfigResponseVO> {

    @Override
    public GroupConfigResponseVO convert(GroupConfig groupConfig) {
        return convert(groupConfig, GroupConfigResponseVO.class);
    }

    @Override
    public List<GroupConfigResponseVO> batchConvert(List<GroupConfig> groupConfigs) {
        return batchConvert(groupConfigs, GroupConfigResponseVO.class);
    }
}
