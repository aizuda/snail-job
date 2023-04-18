package com.aizuda.easy.retry.server.service.impl;

import com.aizuda.easy.retry.server.persistence.mybatis.mapper.NotifyConfigMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.NotifyConfig;
import com.aizuda.easy.retry.server.service.NotifyConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aizuda.easy.retry.server.service.convert.NotifyConfigResponseVOConverter;
import com.aizuda.easy.retry.server.web.model.response.NotifyConfigResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:17
 */
@Service
public class NotifyConfigServiceImpl implements NotifyConfigService {

    private NotifyConfigResponseVOConverter notifyConfigResponseVOConverter = new NotifyConfigResponseVOConverter();

    @Autowired
    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public List<NotifyConfigResponseVO> getNotifyConfigList(String groupName) {
        List<NotifyConfig> notifyConfigs = notifyConfigMapper.selectList(new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getGroupName, groupName));
        return notifyConfigResponseVOConverter.batchConvert(notifyConfigs);
    }
}
