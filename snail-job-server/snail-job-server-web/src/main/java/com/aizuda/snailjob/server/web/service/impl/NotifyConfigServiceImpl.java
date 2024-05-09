package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NotifyConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyConfigRequestVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.NotifyConfigResponseVO;
import com.aizuda.snailjob.server.web.service.NotifyConfigService;
import com.aizuda.snailjob.server.web.service.convert.NotifyConfigConverter;
import com.aizuda.snailjob.server.web.service.convert.NotifyConfigResponseVOConverter;
import com.aizuda.snailjob.server.web.service.handler.SyncConfigHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyRecipientMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: opensnail
 * @date : 2022-03-03 11:17
 */
@Service
@RequiredArgsConstructor
public class NotifyConfigServiceImpl implements NotifyConfigService {

    private final AccessTemplate accessTemplate;
    private final NotifyRecipientMapper notifyRecipientMapper;
    private final JobMapper jobMapper;
    private final WorkflowMapper workflowMapper;

    @Override
    public PageResult<List<NotifyConfigResponseVO>> getNotifyConfigList(NotifyConfigQueryVO queryVO) {
        PageDTO<NotifyConfig> pageDTO = new PageDTO<>();

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        LambdaQueryWrapper<NotifyConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotifyConfig::getNamespaceId, userSessionVO.getNamespaceId());

        if (userSessionVO.isUser()) {
            queryWrapper.in(NotifyConfig::getGroupName, userSessionVO.getGroupNames());
        }

        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            queryWrapper.eq(NotifyConfig::getGroupName, queryVO.getGroupName());
        }
        if (StrUtil.isNotBlank(queryVO.getSceneName())) {
            queryWrapper.eq(NotifyConfig::getBusinessId, queryVO.getSceneName());
        }

        queryWrapper.orderByDesc(NotifyConfig::getId);
        List<NotifyConfig> notifyConfigs = accessTemplate.getNotifyConfigAccess().listPage(pageDTO, queryWrapper)
            .getRecords();

        if (CollectionUtils.isEmpty(notifyConfigs)) {
            return new PageResult<>(pageDTO, Lists.newArrayList());
        }

        List<NotifyConfigResponseVO> notifyConfigResponseVOS = NotifyConfigResponseVOConverter.INSTANCE.batchConvert(
            notifyConfigs);

        Map<Long, String> recipientNameMap = getRecipientNameMap(notifyConfigResponseVOS);
        Map<Long, String> jobNameMap = getJobNameMap(notifyConfigResponseVOS);
        Map<Long, String> workflowNameMap = getWorkflowNameMap(notifyConfigResponseVOS);
        for (final NotifyConfigResponseVO notifyConfigResponseVO : notifyConfigResponseVOS) {
            notifyConfigResponseVO.setRecipientNames(StreamUtils.toSet(notifyConfigResponseVO.getRecipientIds(),
                recipientId -> recipientNameMap.getOrDefault(recipientId, StrUtil.EMPTY)));

            if (Objects.equals(notifyConfigResponseVO.getSystemTaskType(), SyetemTaskTypeEnum.RETRY.getType()) ||
                Objects.equals(notifyConfigResponseVO.getSystemTaskType(), SyetemTaskTypeEnum.CALLBACK.getType())) {
                notifyConfigResponseVO.setBusinessName(notifyConfigResponseVO.getBusinessId());
            } else if (Objects.equals(notifyConfigResponseVO.getSystemTaskType(), SyetemTaskTypeEnum.JOB.getType())) {
                notifyConfigResponseVO.setBusinessName(
                    jobNameMap.get(Long.parseLong(notifyConfigResponseVO.getBusinessId())));
            } else if (Objects.equals(notifyConfigResponseVO.getSystemTaskType(),
                SyetemTaskTypeEnum.WORKFLOW.getType())) {
                notifyConfigResponseVO.setBusinessName(
                    workflowNameMap.get(Long.parseLong(notifyConfigResponseVO.getBusinessId())));
            }
        }

        return new PageResult<>(pageDTO, notifyConfigResponseVOS);
    }

    private Map<Long, String> getWorkflowNameMap(final List<NotifyConfigResponseVO> notifyConfigResponseVOS) {
        Set<Long> workflowIds = notifyConfigResponseVOS.stream().filter(responseVO ->
                responseVO.getSystemTaskType().equals(SyetemTaskTypeEnum.WORKFLOW.getType()))
            .map(responseVO -> Long.parseLong(responseVO.getBusinessId()))
            .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(workflowIds)) {
            List<Workflow> workflows = workflowMapper.selectBatchIds(workflowIds);
            return StreamUtils.toMap(workflows, Workflow::getId, Workflow::getWorkflowName);
        }

        return new HashMap<>();
    }

    private Map<Long, String> getJobNameMap(final List<NotifyConfigResponseVO> notifyConfigResponseVOS) {
        Set<Long> jobIds = notifyConfigResponseVOS.stream().filter(responseVO ->
                responseVO.getSystemTaskType().equals(SyetemTaskTypeEnum.JOB.getType()))
            .map(responseVO -> Long.parseLong(responseVO.getBusinessId()))
            .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(jobIds)) {
            List<Job> jobs = jobMapper.selectBatchIds(jobIds);
            return StreamUtils.toMap(jobs, Job::getId, Job::getJobName);
        }

        return new HashMap<>();
    }

    @NotNull
    private Map<Long, String> getRecipientNameMap(final List<NotifyConfigResponseVO> notifyConfigResponseVOS) {
        Set<Long> recipientIds = notifyConfigResponseVOS.stream()
            .flatMap(config -> config.getRecipientIds().stream())
            .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(recipientIds)) {
            return Maps.newHashMap();
        }

        List<NotifyRecipient> notifyRecipients = notifyRecipientMapper.selectBatchIds(recipientIds);
        return StreamUtils.toMap(notifyRecipients, NotifyRecipient::getId, NotifyRecipient::getRecipientName);
    }

    @Override
    public Boolean saveNotify(NotifyConfigRequestVO requestVO) {
        NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.toNotifyConfig(requestVO);
        notifyConfig.setCreateDt(LocalDateTime.now());
        notifyConfig.setRecipientIds(JsonUtil.toJsonString(requestVO.getRecipientIds()));
        notifyConfig.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
        ConfigAccess<NotifyConfig> notifyConfigAccess = accessTemplate.getNotifyConfigAccess();

        Assert.isTrue(1 == notifyConfigAccess.insert(notifyConfig),
            () -> new SnailJobServerException("failed to insert notify. sceneConfig:[{}]",
                JsonUtil.toJsonString(notifyConfig)));
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateNotify(NotifyConfigRequestVO requestVO) {
        Assert.notNull(requestVO.getId(), () -> new SnailJobServerException("参数异常"));
        NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.toNotifyConfig(requestVO);
        notifyConfig.setRecipientIds(JsonUtil.toJsonString(requestVO.getRecipientIds()));

        // 防止被覆盖
        notifyConfig.setNamespaceId(null);
        Assert.isTrue(1 == accessTemplate.getNotifyConfigAccess().updateById(notifyConfig),
            () -> new SnailJobServerException("failed to update notify. sceneConfig:[{}]",
                JsonUtil.toJsonString(notifyConfig)));
        return Boolean.TRUE;
    }

    @Override
    public NotifyConfigResponseVO getNotifyConfigDetail(Long id) {
        NotifyConfig notifyConfig = accessTemplate.getNotifyConfigAccess().one(new LambdaQueryWrapper<NotifyConfig>()
            .eq(NotifyConfig::getId, id));
        return NotifyConfigResponseVOConverter.INSTANCE.convert(notifyConfig);
    }

    @Override
    public Boolean updateStatus(final Long id, final Integer status) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        NotifyConfig notifyConfig = accessTemplate.getNotifyConfigAccess().one(
            new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getId, id)
                .eq(NotifyConfig::getNamespaceId, namespaceId)
        );
        Assert.notNull(notifyConfig, () -> new SnailJobServerException("通知配置不存在"));

        // 同步配置到客户端
        SyncConfigHandler.addSyncTask(notifyConfig.getGroupName(), namespaceId);

        NotifyConfig config = new NotifyConfig();
        config.setNotifyStatus(status);
        config.setUpdateDt(LocalDateTime.now());
        int update = accessTemplate.getNotifyConfigAccess()
            .update(config, new LambdaUpdateWrapper<NotifyConfig>()
                .eq(NotifyConfig::getNamespaceId, namespaceId)
                .eq(NotifyConfig::getId, id)
            );

        return 1 == update;
    }

    @Override
    public Boolean batchDeleteNotify(final Set<Long> ids) {
        return ids.size() == accessTemplate.getNotifyConfigAccess()
            .delete(new LambdaQueryWrapper<NotifyConfig>().in(NotifyConfig::getId, ids));
    }
}
