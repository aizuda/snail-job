package com.x.retry.server.service.convert;

import com.x.retry.common.core.covert.AbstractConverter;
import com.x.retry.server.persistence.mybatis.po.NotifyConfig;
import com.x.retry.server.web.model.request.GroupConfigRequestVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-26 13:43
 */
public class NotifyConfigConverter extends AbstractConverter<GroupConfigRequestVO.NotifyConfigVO, NotifyConfig> {

    @Override
    public NotifyConfig convert(GroupConfigRequestVO.NotifyConfigVO notifyConfigVO) {

        NotifyConfig notifyConfig = convert(notifyConfigVO, NotifyConfig.class);
        notifyConfig.setUpdateDt(LocalDateTime.now());

        return notifyConfig;
    }

    @Override
    public List<NotifyConfig> batchConvert(List<GroupConfigRequestVO.NotifyConfigVO> notifyConfigVOS) {
        return null;
    }
}
