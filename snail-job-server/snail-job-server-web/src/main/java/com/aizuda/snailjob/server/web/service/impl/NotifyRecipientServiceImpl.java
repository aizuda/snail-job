package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.ExportNotifyRecipientVO;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientRequestVO;
import com.aizuda.snailjob.server.web.model.response.CommonLabelValueResponseVO;
import com.aizuda.snailjob.server.web.model.response.NotifyRecipientResponseVO;
import com.aizuda.snailjob.server.web.service.NotifyRecipientService;
import com.aizuda.snailjob.server.web.service.convert.NotifyRecipientConverter;
import com.aizuda.snailjob.server.web.service.handler.GroupHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyRecipientMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
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
@Validated
public class NotifyRecipientServiceImpl implements NotifyRecipientService {

    private final NotifyRecipientMapper notifyRecipientMapper;
    private final GroupHandler groupHandler;

    @Override
    public PageResult<List<NotifyRecipientResponseVO>> getNotifyRecipientPageList(NotifyRecipientQueryVO queryVO) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        PageDTO<NotifyRecipient> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        PageDTO<NotifyRecipient> notifyRecipientPageDTO = notifyRecipientMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<NotifyRecipient>()
                        .eq(NotifyRecipient::getNamespaceId, namespaceId)
                        .eq(Objects.nonNull(queryVO.getNotifyType()), NotifyRecipient::getNotifyType, queryVO.getNotifyType())
                        .likeRight(StrUtil.isNotBlank(queryVO.getRecipientName()), NotifyRecipient::getRecipientName,
                                queryVO.getRecipientName())
                        .orderByDesc(NotifyRecipient::getCreateDt));

        return new PageResult<>(pageDTO,
                NotifyRecipientConverter.INSTANCE.convertList(notifyRecipientPageDTO.getRecords()));
    }

    @Override
    public Boolean saveNotifyRecipient(NotifyRecipientRequestVO requestVO) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        NotifyRecipient notifyRecipient = NotifyRecipientConverter.INSTANCE.convert(requestVO);
        notifyRecipient.setNamespaceId(namespaceId);
        notifyRecipient.setId(null);
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
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        List<NotifyRecipient> notifyRecipients = notifyRecipientMapper.selectList(
                new LambdaQueryWrapper<NotifyRecipient>()
                        .select(NotifyRecipient::getRecipientName, NotifyRecipient::getId)
                        .eq(NotifyRecipient::getNamespaceId, namespaceId)
        );
        return NotifyRecipientConverter.INSTANCE.convertListToCommonLabelValueList(notifyRecipients);
    }

    @Override
    public Boolean batchDeleteByIds(final Set<Long> ids) {
        return ids.size() == notifyRecipientMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional
    public void importNotifyRecipient(final List<NotifyRecipientRequestVO> notifyRecipientRequestVOS) {
        for (final NotifyRecipientRequestVO notifyRecipientRequestVO : notifyRecipientRequestVOS) {
            this.saveNotifyRecipient(notifyRecipientRequestVO);
        }
    }

    @Override
    public String exportNotifyRecipient(final ExportNotifyRecipientVO exportVO) {

        List<NotifyRecipientRequestVO> requestList = new ArrayList<>();
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        PartitionTaskUtils.process(startId -> {
            List<NotifyRecipient> recipients = notifyRecipientMapper.selectPage(new PageDTO<>(0, 100, Boolean.FALSE),
                    new LambdaQueryWrapper<NotifyRecipient>()
                            .eq(NotifyRecipient::getNamespaceId, namespaceId)
                            .eq(Objects.nonNull(exportVO.getNotifyType()), NotifyRecipient::getNotifyType,
                                    exportVO.getNotifyType())
                            .likeRight(StrUtil.isNotBlank(exportVO.getRecipientName()), NotifyRecipient::getRecipientName,
                                    exportVO.getRecipientName())
                            .ge(NotifyRecipient::getId, startId)
                            .in(CollUtil.isNotEmpty(exportVO.getNotifyRecipientIds()), NotifyRecipient::getId,
                                    exportVO.getNotifyRecipientIds())
                            .orderByAsc(NotifyRecipient::getId)).getRecords();
            return StreamUtils.toList(recipients, NotifyRecipientPartitionTask::new);
        }, partitionTasks -> {
            List<NotifyRecipientPartitionTask> partitionTaskList = (List<NotifyRecipientPartitionTask>) partitionTasks;
            List<NotifyRecipientRequestVO> notifyRecipientRequestVOs = NotifyRecipientConverter.INSTANCE.toNotifyRecipientRequestVOs(
                    StreamUtils.toList(partitionTaskList, NotifyRecipientPartitionTask::getRecipient));
            requestList.addAll(notifyRecipientRequestVOs);
        }, 0);

        return JsonUtil.toJsonString(requestList);
    }

    @EqualsAndHashCode(callSuper = true)
    @Getter
    private static class NotifyRecipientPartitionTask extends PartitionTask {

        // 这里就直接放NotifyRecipient为了后面若加字段不需要再这里在调整了
        private final NotifyRecipient recipient;

        public NotifyRecipientPartitionTask(@NotNull NotifyRecipient recipient) {
            this.recipient = recipient;
            setId(recipient.getId());
        }
    }
}
