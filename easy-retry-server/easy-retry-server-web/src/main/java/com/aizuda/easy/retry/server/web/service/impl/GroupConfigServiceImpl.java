package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.enums.IdGeneratorMode;
import com.aizuda.easy.retry.server.common.enums.SystemModeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.retry.task.support.handler.ConfigVersionSyncHandler;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import com.aizuda.easy.retry.server.web.model.response.GroupConfigResponseVO;
import com.aizuda.easy.retry.server.web.service.GroupConfigService;
import com.aizuda.easy.retry.server.web.service.convert.GroupConfigConverter;
import com.aizuda.easy.retry.server.web.service.convert.GroupConfigResponseVOConverter;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.ConfigAccess;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.SequenceAllocMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CRUD 组、场景、通知
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-22 14:54
 * @since 1.0.0
 */
@Service
public class GroupConfigServiceImpl implements GroupConfigService {

    @Autowired
    private ServerNodeMapper serverNodeMapper;
    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private SequenceAllocMapper sequenceAllocMapper;
    @Autowired
    @Lazy
    private ConfigVersionSyncHandler configVersionSyncHandler;

    @Autowired
    private SystemProperties systemProperties;

    @Override
    @Transactional
    public Boolean addGroup(UserSessionVO systemUser, GroupConfigRequestVO groupConfigRequestVO) {

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        Assert.isTrue(groupConfigAccess.count(new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName())) == 0,
                () -> new EasyRetryServerException("GroupName已经存在 {}", groupConfigRequestVO.getGroupName()));

        // 保存组配置
        doSaveGroupConfig(systemUser, groupConfigRequestVO);

        // 保存生成唯一id配置
        doSaveSequenceAlloc(systemUser, groupConfigRequestVO);

        return Boolean.TRUE;
    }

    /**
     * 保存序号生成规则配置失败
     *
     * @param systemUser
     * @param groupConfigRequestVO 组、场景、通知配置类
     */
    private void doSaveSequenceAlloc(UserSessionVO systemUser, final GroupConfigRequestVO groupConfigRequestVO) {
        SequenceAlloc sequenceAlloc = new SequenceAlloc();
        sequenceAlloc.setGroupName(groupConfigRequestVO.getGroupName());
        sequenceAlloc.setNamespaceId(systemUser.getNamespaceId());
        sequenceAlloc.setStep(systemProperties.getStep());
        sequenceAlloc.setUpdateDt(LocalDateTime.now());
        Assert.isTrue(1 == sequenceAllocMapper.insert(sequenceAlloc), () -> new EasyRetryServerException("failed to save sequence generation rule configuration [{}].", groupConfigRequestVO.getGroupName()));
    }

    @Override
    @Transactional
    public Boolean updateGroup(GroupConfigRequestVO groupConfigRequestVO) {

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        long count = groupConfigAccess.count(
                new LambdaQueryWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName()));
        if (count <= 0) {
            return false;
        }

        GroupConfig groupConfig = GroupConfigConverter.INSTANCE.convert(groupConfigRequestVO);
        groupConfig.setDescription(Optional.ofNullable(groupConfigRequestVO.getDescription()).orElse(StrUtil.EMPTY));
        // 使用@TableField(value = "version", update= "%s+1") 进行更新version, 这里必须初始化一个值
        groupConfig.setVersion(1);
        Assert.isTrue(systemProperties.getTotalPartition() > groupConfigRequestVO.getGroupPartition(), () -> new EasyRetryServerException("分区超过最大分区. [{}]", systemProperties.getTotalPartition() - 1));
        Assert.isTrue(groupConfigRequestVO.getGroupPartition() >= 0, () -> new EasyRetryServerException("分区不能是负数."));

        // 校验retry_task_x和retry_dead_letter_x是否存在
        checkGroupPartition(groupConfig);

        Assert.isTrue(1 == groupConfigAccess.update(groupConfig,
                        new LambdaUpdateWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName())),
                () -> new EasyRetryServerException("exception occurred while adding group. groupConfigVO[{}]", groupConfigRequestVO));


        if (SystemModeEnum.isRetry(systemProperties.getMode())) {
            // 同步版本， 版本为0代表需要同步到客户端
            boolean add = configVersionSyncHandler.addSyncTask(groupConfigRequestVO.getGroupName(), 0);
            // 若添加失败则强制发起同步
            if (!add) {
                configVersionSyncHandler.syncVersion(groupConfigRequestVO.getGroupName());
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean updateGroupStatus(String groupName, Integer status) {
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setGroupStatus(status);
        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        return groupConfigAccess.update(groupConfig, new LambdaUpdateWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupName)) == 1;
    }

    @Override
    public PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO) {

        LambdaQueryWrapper<GroupConfig> groupConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        groupConfigLambdaQueryWrapper.eq(GroupConfig::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId());
        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            groupConfigLambdaQueryWrapper.like(GroupConfig::getGroupName, queryVO.getGroupName() + "%");
        }

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();

        groupConfigLambdaQueryWrapper.orderByDesc(GroupConfig::getId);
        PageDTO<GroupConfig> groupConfigPageDTO = groupConfigAccess.listPage(new PageDTO<>(queryVO.getPage(), queryVO.getSize()), groupConfigLambdaQueryWrapper);
        List<GroupConfig> records = groupConfigPageDTO.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PageResult<>(groupConfigPageDTO.getCurrent(), groupConfigPageDTO.getSize(), groupConfigPageDTO.getTotal());
        }

        PageResult<List<GroupConfigResponseVO>> pageResult = new PageResult<>(groupConfigPageDTO.getCurrent(), groupConfigPageDTO.getSize(), groupConfigPageDTO.getTotal());

        List<GroupConfigResponseVO> responseVOList = GroupConfigResponseVOConverter.INSTANCE.toGroupConfigResponseVO(records);

        for (GroupConfigResponseVO groupConfigResponseVO : responseVOList) {
            List<ServerNode> serverNodes = serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getGroupName, groupConfigResponseVO.getGroupName()));
            groupConfigResponseVO.setOnlinePodList(serverNodes.stream().map(serverNode -> serverNode.getHostIp() + ":" + serverNode.getHostPort()).collect(Collectors.toList()));
            Optional.ofNullable(IdGeneratorMode.modeOf(groupConfigResponseVO.getIdGeneratorMode())).ifPresent(idGeneratorMode -> {
                groupConfigResponseVO.setIdGeneratorModeName(idGeneratorMode.getDesc());
            });
        }

        pageResult.setData(responseVOList);

        return pageResult;
    }

    private void doSaveGroupConfig(UserSessionVO systemUser, GroupConfigRequestVO groupConfigRequestVO) {
        GroupConfig groupConfig = GroupConfigConverter.INSTANCE.convert(groupConfigRequestVO);
        groupConfig.setCreateDt(LocalDateTime.now());
        groupConfig.setVersion(1);
        groupConfig.setNamespaceId(systemUser.getNamespaceId());
        groupConfig.setGroupName(groupConfigRequestVO.getGroupName());
        groupConfig.setDescription(Optional.ofNullable(groupConfigRequestVO.getDescription()).orElse(StrUtil.EMPTY));
        if (Objects.isNull(groupConfigRequestVO.getGroupPartition())) {
            groupConfig.setGroupPartition(HashUtil.bkdrHash(groupConfigRequestVO.getGroupName()) % systemProperties.getTotalPartition());
        } else {
            Assert.isTrue(systemProperties.getTotalPartition() > groupConfigRequestVO.getGroupPartition(), () -> new EasyRetryServerException("分区超过最大分区. [{}]", systemProperties.getTotalPartition() - 1));
            Assert.isTrue(groupConfigRequestVO.getGroupPartition() >= 0, () -> new EasyRetryServerException("分区不能是负数."));
        }

        groupConfig.setBucketIndex(HashUtil.bkdrHash(groupConfigRequestVO.getGroupName()) % systemProperties.getBucketTotal());
        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        Assert.isTrue(1 == groupConfigAccess.insert(groupConfig), () -> new EasyRetryServerException("新增组异常异常 groupConfigVO[{}]", groupConfigRequestVO));

        // 校验retry_task_x和retry_dead_letter_x是否存在
        checkGroupPartition(groupConfig);
    }

    /**
     * 校验retry_task_x和retry_dead_letter_x是否存在
     */
    private void checkGroupPartition(GroupConfig groupConfig) {
        try {
            TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
            retryTaskAccess.one(groupConfig.getGroupName(), new LambdaQueryWrapper<RetryTask>().eq(RetryTask::getId, 1));
        } catch (BadSqlGrammarException e) {
            Optional.ofNullable(e.getMessage()).ifPresent(s -> {
                if (s.contains("retry_task_" + groupConfig.getGroupPartition()) && s.contains("doesn't exist")) {
                    throw new EasyRetryServerException("分区:[{}] '未配置表retry_task_{}', 请联系管理员进行配置", groupConfig.getGroupPartition(), groupConfig.getGroupPartition());
                }
            });
        }

        try {
            TaskAccess<RetryDeadLetter> retryTaskAccess = accessTemplate.getRetryDeadLetterAccess();
            retryTaskAccess.one(groupConfig.getGroupName(), new LambdaQueryWrapper<RetryDeadLetter>().eq(RetryDeadLetter::getId, 1));
        } catch (BadSqlGrammarException e) {
            Optional.ofNullable(e.getMessage()).ifPresent(s -> {
                if (s.contains("retry_dead_letter_" + groupConfig.getGroupPartition()) && s.contains("doesn't exist")) {
                    throw new EasyRetryServerException("分区:[{}] '未配置表retry_dead_letter_{}', 请联系管理员进行配置", groupConfig.getGroupPartition(), groupConfig.getGroupPartition());
                }
            });
        }
    }

    @Override
    public GroupConfigResponseVO getGroupConfigByGroupName(String groupName) {

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        GroupConfig groupConfig = groupConfigAccess.one(new LambdaQueryWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupName));

        GroupConfigResponseVO groupConfigResponseVO = GroupConfigResponseVOConverter.INSTANCE.toGroupConfigResponseVO(groupConfig);

        Optional.ofNullable(IdGeneratorMode.modeOf(groupConfig.getIdGeneratorMode())).ifPresent(idGeneratorMode -> {
            groupConfigResponseVO.setIdGeneratorModeName(idGeneratorMode.getDesc());
        });

        return groupConfigResponseVO;
    }

    @Override
    public List<GroupConfigResponseVO> getAllGroupConfigList(final List<String> namespaceId) {
        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();

        List<GroupConfig> groupConfigs = groupConfigAccess.list(new LambdaQueryWrapper<GroupConfig>()
                        .in(GroupConfig::getNamespaceId, namespaceId)
                        .select(GroupConfig::getGroupName, GroupConfig::getNamespaceId)).stream()
                .collect(Collectors.toList());
        return GroupConfigResponseVOConverter.INSTANCE.toGroupConfigResponseVO(groupConfigs);
    }

    @Override
    public List<String> getAllGroupNameList() {
        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();

        List<GroupConfig> groupConfigs = groupConfigAccess.list(new LambdaQueryWrapper<GroupConfig>()
                        .in(GroupConfig::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
                        .select(GroupConfig::getGroupName)).stream()
                .collect(Collectors.toList());

        return groupConfigs.stream().map(GroupConfig::getGroupName).collect(Collectors.toList());
    }

}
