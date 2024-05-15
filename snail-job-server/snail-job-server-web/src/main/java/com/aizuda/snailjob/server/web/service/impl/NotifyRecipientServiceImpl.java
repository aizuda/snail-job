package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientRequestVO;
import com.aizuda.snailjob.server.web.model.response.CommonLabelValueResponseVO;
import com.aizuda.snailjob.server.web.model.response.NotifyRecipientResponseVO;
import com.aizuda.snailjob.server.web.service.NotifyRecipientService;
import com.aizuda.snailjob.server.web.service.convert.NotifyRecipientConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyRecipientMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    public PageResult<List<NotifyRecipientResponseVO>> getNotifyRecipientPageList(NotifyRecipientQueryVO queryVO) {
        PageDTO<NotifyRecipient> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        PageDTO<NotifyRecipient> notifyRecipientPageDTO = notifyRecipientMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<NotifyRecipient>()
                        .likeRight(StrUtil.isNotBlank(queryVO.getRecipientName()), NotifyRecipient::getRecipientName, queryVO.getRecipientName())
                        .likeRight(Objects.nonNull(queryVO.getNotifyType()), NotifyRecipient::getNotifyType, queryVO.getNotifyType())
                        .orderByDesc(NotifyRecipient::getCreateDt));

        return new PageResult<>(pageDTO,
                NotifyRecipientConverter.INSTANCE.convertList(notifyRecipientPageDTO.getRecords()));
    }

    @Override
    public Boolean saveNotifyRecipient(NotifyRecipientRequestVO requestVO) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        NotifyRecipient notifyRecipient = NotifyRecipientConverter.INSTANCE.convert(requestVO);
        notifyRecipient.setNamespaceId(namespaceId);
        return 1 == notifyRecipientMapper.insert(notifyRecipient);
    }

    @Override
    public Boolean updateNotifyRecipient(NotifyRecipientRequestVO requestVO) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        NotifyRecipient notifyRecipient = NotifyRecipientConverter.INSTANCE.convert(requestVO);
        notifyRecipient.setNamespaceId(namespaceId);
        return 1 == notifyRecipientMapper.updateById(notifyRecipient);
    }

    @Override
    public List<CommonLabelValueResponseVO> getNotifyRecipientList() {
        List<NotifyRecipient> notifyRecipients = notifyRecipientMapper.selectList(
                new LambdaQueryWrapper<NotifyRecipient>()
                        .select(NotifyRecipient::getRecipientName, NotifyRecipient::getId));
        return NotifyRecipientConverter.INSTANCE.convertListToCommonLabelValueList(notifyRecipients);
    }

    @Override
    public Boolean batchDeleteByIds(final Set<Long> ids) {
        return ids.size() == notifyRecipientMapper.deleteBatchIds(ids);
    }
}
