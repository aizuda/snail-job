package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.enums.IdGeneratorMode;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.retry.task.support.handler.ConfigVersionSyncHandler;
import com.aizuda.easy.retry.server.web.service.GroupConfigService;
import com.aizuda.easy.retry.server.web.service.convert.GroupConfigConverter;
import com.aizuda.easy.retry.server.web.service.convert.GroupConfigResponseVOConverter;
import com.aizuda.easy.retry.server.web.service.convert.NotifyConfigConverter;
import com.aizuda.easy.retry.server.web.service.convert.SceneConfigConverter;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.response.GroupConfigResponseVO;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.ConfigAccess;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.*;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
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
    private ConfigVersionSyncHandler configVersionSyncHandler;

    @Value("${easy-retry.total-partition:32}")
    private Integer totalPartition;
    @Value("${easy-retry.step:100}")
    private Integer step;

    @Override
    @Transactional
    public Boolean addGroup(GroupConfigRequestVO groupConfigRequestVO) {

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        Assert.isTrue(groupConfigAccess.count(new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName())) == 0,
                () -> new EasyRetryServerException("GroupName已经存在 {}", groupConfigRequestVO.getGroupName()));

        // 保存组配置
        doSaveGroupConfig(groupConfigRequestVO);

        // 保存生成唯一id配置
        doSaveSequenceAlloc(groupConfigRequestVO);

        // 保存通知配置
        doSaveNotifyConfig(groupConfigRequestVO);

        // 保存场景配置
        doSaveSceneConfig(groupConfigRequestVO.getSceneList(), groupConfigRequestVO.getGroupName());

        return Boolean.TRUE;
    }

    /**
     * 保存序号生成规则配置失败
     *
     * @param groupConfigRequestVO 组、场景、通知配置类
     */
    private void doSaveSequenceAlloc(final GroupConfigRequestVO groupConfigRequestVO) {
        SequenceAlloc sequenceAlloc = new SequenceAlloc();
        sequenceAlloc.setGroupName(groupConfigRequestVO.getGroupName());
        sequenceAlloc.setStep(step);
        sequenceAlloc.setUpdateDt(LocalDateTime.now());
        Assert.isTrue(1 == sequenceAllocMapper.insert(sequenceAlloc), () -> new EasyRetryServerException("failed to save sequence generation rule configuration [{}].", groupConfigRequestVO.getGroupName()));
    }

    @Override
    @Transactional
    public Boolean updateGroup(GroupConfigRequestVO groupConfigRequestVO) {

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        GroupConfig groupConfig = groupConfigAccess.one(
                new LambdaQueryWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName()));
        if (Objects.isNull(groupConfig)) {
            return false;
        }

        groupConfig.setVersion(groupConfig.getVersion() + 1);
        BeanUtils.copyProperties(groupConfigRequestVO, groupConfig);

        Assert.isTrue(totalPartition > groupConfigRequestVO.getGroupPartition(), () -> new EasyRetryServerException("分区超过最大分区. [{}]", totalPartition - 1));
        Assert.isTrue(groupConfigRequestVO.getGroupPartition() >= 0, () -> new EasyRetryServerException("分区不能是负数."));

        // 校验retry_task_x和retry_dead_letter_x是否存在
        checkGroupPartition(groupConfig);

        Assert.isTrue(1 == groupConfigAccess.update(groupConfig,
                        new LambdaUpdateWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName())),
                () -> new EasyRetryServerException("exception occurred while adding group. groupConfigVO[{}]", groupConfigRequestVO));

        doUpdateNotifyConfig(groupConfigRequestVO);

        doUpdateSceneConfig(groupConfigRequestVO);

        // 同步版本， 版本为0代表需要同步到客户端
        boolean add = configVersionSyncHandler.addSyncTask(groupConfigRequestVO.getGroupName(), 0);
        // 若添加失败则强制发起同步
        if (!add) {
            configVersionSyncHandler.syncVersion(groupConfigRequestVO.getGroupName());
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
        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            groupConfigLambdaQueryWrapper.like(GroupConfig::getGroupName, queryVO.getGroupName());
        }

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();

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

    private void doSaveGroupConfig(GroupConfigRequestVO groupConfigRequestVO) {
        GroupConfig groupConfig = GroupConfigConverter.INSTANCE.convert(groupConfigRequestVO);
        groupConfig.setCreateDt(LocalDateTime.now());
        groupConfig.setVersion(1);
        groupConfig.setGroupName(groupConfigRequestVO.getGroupName());
        if (Objects.isNull(groupConfigRequestVO.getGroupPartition())) {
            groupConfig.setGroupPartition(HashUtil.bkdrHash(groupConfigRequestVO.getGroupName()) % totalPartition);
        } else {
            Assert.isTrue(totalPartition > groupConfigRequestVO.getGroupPartition(), () -> new EasyRetryServerException("分区超过最大分区. [{}]", totalPartition - 1));
            Assert.isTrue(groupConfigRequestVO.getGroupPartition() >= 0, () -> new EasyRetryServerException("分区不能是负数."));
        }

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
    public List<String> getAllGroupNameList() {
        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();

        return groupConfigAccess.list(new LambdaQueryWrapper<GroupConfig>()
                        .select(GroupConfig::getGroupName)).stream()
                .map(GroupConfig::getGroupName)
                .collect(Collectors.toList());
    }

    private void doSaveNotifyConfig(GroupConfigRequestVO groupConfigRequestVO) {
        List<GroupConfigRequestVO.NotifyConfigVO> notifyList = groupConfigRequestVO.getNotifyList();
        if (!CollectionUtils.isEmpty(notifyList)) {
            for (GroupConfigRequestVO.NotifyConfigVO notifyConfigVO : notifyList) {
                doSaveNotifyConfig(groupConfigRequestVO.getGroupName(), notifyConfigVO);
            }
        }
    }

    private void doSaveNotifyConfig(String groupName, GroupConfigRequestVO.NotifyConfigVO notifyConfigVO) {

        NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.convert(notifyConfigVO);
        notifyConfig.setGroupName(groupName);
        notifyConfig.setCreateDt(LocalDateTime.now());

        ConfigAccess<NotifyConfig> notifyConfigAccess = accessTemplate.getNotifyConfigAccess();

        Assert.isTrue(1 == notifyConfigAccess.insert(notifyConfig),
                () -> new EasyRetryServerException("failed to insert notify. sceneConfig:[{}]", JsonUtil.toJsonString(notifyConfig)));
    }

    private void doSaveSceneConfig(List<GroupConfigRequestVO.SceneConfigVO> sceneList, String groupName) {
        if (!CollectionUtils.isEmpty(sceneList)) {

            for (GroupConfigRequestVO.SceneConfigVO sceneConfigVO : sceneList) {

                SceneConfig sceneConfig = SceneConfigConverter.INSTANCE.convert(sceneConfigVO);
                sceneConfig.setCreateDt(LocalDateTime.now());
                sceneConfig.setGroupName(groupName);
                ConfigAccess<SceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();

                Assert.isTrue(1 == sceneConfigAccess.insert(sceneConfig),
                        () -> new EasyRetryServerException("failed to insert scene. sceneConfig:[{}]", JsonUtil.toJsonString(sceneConfig)));
            }
        }
    }

    private void doUpdateSceneConfig(GroupConfigRequestVO groupConfigRequestVO) {
        List<GroupConfigRequestVO.SceneConfigVO> sceneList = groupConfigRequestVO.getSceneList();
        if (!CollectionUtils.isEmpty(sceneList)) {

            ConfigAccess<SceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();

            List<SceneConfig> sceneConfigs = sceneConfigAccess.list(new LambdaQueryWrapper<SceneConfig>().eq(SceneConfig::getGroupName, groupConfigRequestVO.getGroupName()));
            Map<String, SceneConfig> sceneConfigMap = sceneConfigs.stream().collect(Collectors.toMap(SceneConfig::getSceneName, i -> i));

            Iterator<GroupConfigRequestVO.SceneConfigVO> iterator = sceneList.iterator();

            while (iterator.hasNext()) {
                GroupConfigRequestVO.SceneConfigVO sceneConfigVO = iterator.next();
                SceneConfig oldSceneConfig = sceneConfigMap.get(sceneConfigVO.getSceneName());
                if (Objects.isNull(oldSceneConfig)) {
                    doSaveSceneConfig(Collections.singletonList(sceneConfigVO), groupConfigRequestVO.getGroupName());

                } else if (sceneConfigVO.getIsDeleted() == 1) {
                    Assert.isTrue(
                            1 == sceneConfigAccess.delete(new LambdaQueryWrapper<SceneConfig>().eq(SceneConfig::getId, oldSceneConfig.getId())),
                            () -> new EasyRetryServerException("failed to delete scene. [{}]", sceneConfigVO.getSceneName()));
                } else {
                    SceneConfig sceneConfig = SceneConfigConverter.INSTANCE.convert(sceneConfigVO);
                    sceneConfig.setGroupName(groupConfigRequestVO.getGroupName());

                    Assert.isTrue(1 == sceneConfigAccess.update(sceneConfig,
                                    new LambdaUpdateWrapper<SceneConfig>()
                                            .eq(SceneConfig::getGroupName, sceneConfig.getGroupName())
                                            .eq(SceneConfig::getSceneName, sceneConfig.getSceneName())),
                            () -> new EasyRetryServerException("failed to update scene. sceneConfig:[{}]", JsonUtil.toJsonString(sceneConfig)));
                }

                iterator.remove();
            }
        }
    }

    private void doUpdateNotifyConfig(GroupConfigRequestVO groupConfigRequestVO) {
        List<GroupConfigRequestVO.NotifyConfigVO> notifyList = groupConfigRequestVO.getNotifyList();

        if (CollectionUtils.isEmpty(notifyList)) {
            return;
        }

        ConfigAccess<NotifyConfig> notifyConfigAccess = accessTemplate.getNotifyConfigAccess();

        for (GroupConfigRequestVO.NotifyConfigVO notifyConfigVO : notifyList) {

            NotifyConfig notifyConfig = NotifyConfigConverter.INSTANCE.convert(notifyConfigVO);
            notifyConfig.setGroupName(groupConfigRequestVO.getGroupName());
            notifyConfig.setCreateDt(LocalDateTime.now());
            if (Objects.isNull(notifyConfigVO.getId())) {
                // insert
                doSaveNotifyConfig(groupConfigRequestVO.getGroupName(), notifyConfigVO);
            } else if (Objects.nonNull(notifyConfigVO.getId()) && notifyConfigVO.getIsDeleted() == 1) {
                // delete
                Assert.isTrue(1 == notifyConfigAccess.delete(new LambdaQueryWrapper<NotifyConfig>()
                                .eq(NotifyConfig::getId, notifyConfigVO.getId())),
                        () -> new EasyRetryServerException("failed to delete notify. sceneConfig:[{}]", JsonUtil.toJsonString(notifyConfigVO)));
            } else {
                // update
                Assert.isTrue(1 == notifyConfigAccess.update(notifyConfig,
                                new LambdaUpdateWrapper<NotifyConfig>()
                                        .eq(NotifyConfig::getId, notifyConfigVO.getId())
                                        .eq(NotifyConfig::getGroupName, notifyConfig.getGroupName())
                                        .eq(NotifyConfig::getNotifyScene, notifyConfig.getNotifyScene())),
                        () -> new EasyRetryServerException("failed to update notify. sceneConfig:[{}]", JsonUtil.toJsonString(notifyConfig)));
            }
        }
    }

}
