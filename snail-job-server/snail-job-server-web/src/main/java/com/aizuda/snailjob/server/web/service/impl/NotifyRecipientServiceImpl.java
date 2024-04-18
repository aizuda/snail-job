package com.aizuda.snailjob.server.web.service.impl;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientRequestVO;
import com.aizuda.snailjob.server.web.model.response.NotifyRecipientResponseVO;
import com.aizuda.snailjob.server.web.service.NotifyRecipientService;
import com.aizuda.snailjob.server.web.service.convert.NotifyRecipientConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyRecipientMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author opensnail
 * @date 2024-04-17 21:24:51
 * @since sj_1.0.0
 */
@Service
@RequiredArgsConstructor
public class NotifyRecipientServiceImpl implements NotifyRecipientService {
    private final NotifyRecipientMapper notifyRecipientMapper;

    @Override
    public PageResult<List<NotifyRecipientResponseVO>> getNotifyRecipientList(NotifyRecipientQueryVO queryVO) {
        PageDTO<NotifyRecipient> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<NotifyRecipient> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(NotifyRecipient::getCreateDt);
        PageDTO<NotifyRecipient> notifyRecipientPageDTO = notifyRecipientMapper.selectPage(pageDTO, queryWrapper);

        return new PageResult<>(pageDTO, NotifyRecipientConverter.INSTANCE.toNotifyRecipientResponseVOs(notifyRecipientPageDTO.getRecords()));
    }

    @Override
    public Boolean saveNotifyRecipient(NotifyRecipientRequestVO requestVO) {
        return 1 == notifyRecipientMapper.insert(NotifyRecipientConverter.INSTANCE.toNotifyRecipient(requestVO));
    }

    @Override
    public Boolean updateNotifyRecipient(NotifyRecipientRequestVO requestVO) {
        return 1 == notifyRecipientMapper.updateById(NotifyRecipientConverter.INSTANCE.toNotifyRecipient(requestVO));
    }
}