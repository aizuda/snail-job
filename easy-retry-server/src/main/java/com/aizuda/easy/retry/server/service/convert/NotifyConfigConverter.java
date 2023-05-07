package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.NotifyConfig;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-26 13:43
 */
@Mapper
public interface NotifyConfigConverter {

    NotifyConfigConverter INSTANCE = Mappers.getMapper(NotifyConfigConverter.class);

    NotifyConfig convert(GroupConfigRequestVO.NotifyConfigVO notifyConfigVO);

    List<NotifyConfig> batchConvert(List<GroupConfigRequestVO.NotifyConfigVO> notifyConfigVOS);
}
