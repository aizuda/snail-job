package com.x.retry.server.service.impl;

import cn.hutool.core.util.HashUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.x.retry.common.core.enums.NodeTypeEnum;
import com.x.retry.common.core.enums.NotifySceneEnum;
import com.x.retry.common.core.util.Assert;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.server.exception.XRetryServerException;
import com.x.retry.server.model.dto.ConfigDTO;
import com.x.retry.server.persistence.mybatis.mapper.GroupConfigMapper;
import com.x.retry.server.persistence.mybatis.mapper.NotifyConfigMapper;
import com.x.retry.server.persistence.mybatis.mapper.SceneConfigMapper;
import com.x.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.x.retry.server.persistence.mybatis.po.GroupConfig;
import com.x.retry.server.persistence.mybatis.po.NotifyConfig;
import com.x.retry.server.persistence.mybatis.po.SceneConfig;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import com.x.retry.server.persistence.support.ConfigAccess;
import com.x.retry.server.service.GroupConfigService;
import com.x.retry.server.service.convert.GroupConfigConverter;
import com.x.retry.server.service.convert.GroupConfigResponseVOConverter;
import com.x.retry.server.service.convert.NotifyConfigConverter;
import com.x.retry.server.service.convert.SceneConfigConverter;
import com.x.retry.server.support.handler.ClientRegisterHandler;
import com.x.retry.server.web.model.base.PageResult;
import com.x.retry.server.web.model.request.GroupConfigQueryVO;
import com.x.retry.server.web.model.request.GroupConfigRequestVO;
import com.x.retry.server.web.model.response.GroupConfigResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-22 14:54
 */
@Service
public class GroupConfigServiceImpl implements GroupConfigService {

    @Autowired
    private GroupConfigMapper groupConfigMapper;
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;
    @Autowired
    private SceneConfigMapper sceneConfigMapper;
    @Autowired
    private ServerNodeMapper serverNodeMapper;
    @Autowired
    private ClientRegisterHandler clientRegisterHandler;

    @Value("${x-retry.total-partition:32}")
    private Integer totalPartition;

    private GroupConfigConverter groupConfigConverter = new GroupConfigConverter();
    private NotifyConfigConverter notifyConfigConverter = new NotifyConfigConverter();
    private SceneConfigConverter sceneConfigConverter = new SceneConfigConverter();
    private GroupConfigResponseVOConverter groupConfigResponseVOConverter = new GroupConfigResponseVOConverter();

    @Override
    @Transactional
    public Boolean addGroup(GroupConfigRequestVO groupConfigRequestVO) {

        Assert.isTrue(groupConfigMapper.selectCount(new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName())) == 0,
                new XRetryServerException("GroupName已经存在 {}", groupConfigRequestVO.getGroupName()));

        doSaveGroupConfig(groupConfigRequestVO);

        doSaveNotifyConfig(groupConfigRequestVO);

        doSaveSceneConfig(groupConfigRequestVO.getSceneList(), groupConfigRequestVO.getGroupName());

        return Boolean.TRUE;
    }


    @Override
    @Transactional
    public Boolean updateGroup(GroupConfigRequestVO groupConfigRequestVO) {

        Assert.isTrue(1 == groupConfigMapper.update(groupConfigConverter.convert(groupConfigRequestVO),
                new LambdaUpdateWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName())),
                new XRetryServerException("新增组异常异常 groupConfigVO[{}]", groupConfigRequestVO));

        doUpdateNotifyConfig(groupConfigRequestVO);

        doUpdateSceneConfig(groupConfigRequestVO);

        List<ServerNode> serverNodes = serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getGroupName, groupConfigRequestVO.getGroupName()));
        for (ServerNode serverNode : serverNodes) {
            clientRegisterHandler.syncVersion(null,
                    groupConfigRequestVO.getGroupName(), serverNode.getHostIp(),
                    serverNode.getHostPort());
        }

        return Boolean.TRUE;
    }

    @Override
    public PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO) {

        LambdaQueryWrapper<GroupConfig> groupConfigLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(queryVO.getGroupName())) {
            groupConfigLambdaQueryWrapper.like(GroupConfig::getGroupName, queryVO.getGroupName());
        }

        PageDTO<GroupConfig> groupConfigPageDTO = groupConfigMapper.selectPage(new PageDTO<>(queryVO.getPage(), queryVO.getSize()), groupConfigLambdaQueryWrapper);
        List<GroupConfig> records = groupConfigPageDTO.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PageResult<>(groupConfigPageDTO.getCurrent(), groupConfigPageDTO.getSize(), groupConfigPageDTO.getTotal());
        }

        PageResult<List<GroupConfigResponseVO>> pageResult = new PageResult<>(groupConfigPageDTO.getCurrent(), groupConfigPageDTO.getSize(), groupConfigPageDTO.getTotal());


        List<GroupConfigResponseVO> responseVOList = groupConfigResponseVOConverter.batchConvert(records);
        for (GroupConfigResponseVO groupConfigResponseVO : responseVOList) {
            List<ServerNode> serverNodes = serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getGroupName, groupConfigResponseVO.getGroupName()));
            groupConfigResponseVO.setOnlinePodList(serverNodes.stream().map(serverNode -> serverNode.getHostIp() + ":" + serverNode.getHostPort()).collect(Collectors.toList()));
        }

        pageResult.setData(responseVOList);

        return pageResult;
    }

    private void doSaveGroupConfig(GroupConfigRequestVO groupConfigRequestVO) {
        GroupConfig groupConfig = groupConfigConverter.convert(groupConfigRequestVO);
        groupConfig.setCreateDt(LocalDateTime.now());
        groupConfig.setVersion(1);
        groupConfig.setGroupName(groupConfigRequestVO.getGroupName());
        if (Objects.isNull(groupConfigRequestVO.getGroupPartition())) {
            groupConfig.setGroupPartition(HashUtil.bkdrHash(groupConfigRequestVO.getGroupName()) % totalPartition);
        }

        Assert.isTrue(1 == groupConfigMapper.insert(groupConfig), new XRetryServerException("新增组异常异常 groupConfigVO[{}]", groupConfigRequestVO));
    }

    @Override
    public GroupConfigResponseVO getGroupConfigByGroupName(String groupName) {
        GroupConfig groupConfig = groupConfigMapper.selectOne(new LambdaQueryWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupName));
        return groupConfigResponseVOConverter.convert(groupConfig);
    }

    @Override
    public List<String> getAllGroupNameList() {
        return groupConfigMapper.selectList(new LambdaQueryWrapper<GroupConfig>()
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

        NotifyConfig notifyConfig = notifyConfigConverter.convert(notifyConfigVO);
        notifyConfig.setGroupName(groupName);
        notifyConfig.setCreateDt(LocalDateTime.now());

        Assert.isTrue(1 == notifyConfigMapper.insert(notifyConfig),
                new XRetryServerException("插入通知配置失败 sceneConfig:[{}]", JsonUtil.toJsonString(notifyConfig)));
    }

    private void doSaveSceneConfig(List<GroupConfigRequestVO.SceneConfigVO> sceneList, String groupName) {
        if (!CollectionUtils.isEmpty(sceneList)) {

            for (GroupConfigRequestVO.SceneConfigVO sceneConfigVO : sceneList) {

                SceneConfig sceneConfig = sceneConfigConverter.convert(sceneConfigVO);
                sceneConfig.setCreateDt(LocalDateTime.now());
                sceneConfig.setGroupName(groupName);

                Assert.isTrue(1 == sceneConfigMapper.insert(sceneConfig),
                        new XRetryServerException("插入场景配置失败 sceneConfig:[{}]", JsonUtil.toJsonString(sceneConfig)));
            }
        }
    }

    private void doUpdateSceneConfig(GroupConfigRequestVO groupConfigRequestVO) {
        List<GroupConfigRequestVO.SceneConfigVO> sceneList = groupConfigRequestVO.getSceneList();
        if (!CollectionUtils.isEmpty(sceneList)) {

            List<SceneConfig> sceneConfigs = sceneConfigMapper.selectList(new LambdaQueryWrapper<SceneConfig>().eq(SceneConfig::getGroupName, groupConfigRequestVO.getGroupName()));
            Map<String, SceneConfig> sceneConfigMap = sceneConfigs.stream().collect(Collectors.toMap(SceneConfig::getSceneName, i -> i));

            Iterator<GroupConfigRequestVO.SceneConfigVO> iterator = sceneList.iterator();

            while (iterator.hasNext()) {
                GroupConfigRequestVO.SceneConfigVO sceneConfigVO = iterator.next();
                SceneConfig oldSceneConfig = sceneConfigMap.get(sceneConfigVO.getSceneName());
                if (Objects.isNull(oldSceneConfig)) {
                    doSaveSceneConfig(Collections.singletonList(sceneConfigVO), groupConfigRequestVO.getGroupName());

                } else if (sceneConfigVO.getIsDeleted() == 1) {
                    Assert.isTrue(
                            1 == sceneConfigMapper.deleteById(oldSceneConfig.getId()),
                            new XRetryServerException("删除场景失败 [{}]", sceneConfigVO.getSceneName()));
                } else {
                    SceneConfig sceneConfig = sceneConfigConverter.convert(sceneConfigVO);
                    sceneConfig.setGroupName(groupConfigRequestVO.getGroupName());

                    Assert.isTrue(1 == sceneConfigMapper.update(sceneConfig,
                            new LambdaQueryWrapper<SceneConfig>()
                                    .eq(SceneConfig::getGroupName, sceneConfig.getGroupName())
                                    .eq(SceneConfig::getSceneName, sceneConfig.getSceneName())),
                            new XRetryServerException("插入场景配置失败 sceneConfig:[{}]", JsonUtil.toJsonString(sceneConfig)));
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

        for (GroupConfigRequestVO.NotifyConfigVO notifyConfigVO : notifyList) {

            NotifyConfig notifyConfig = notifyConfigConverter.convert(notifyConfigVO);
            notifyConfig.setGroupName(groupConfigRequestVO.getGroupName());
            notifyConfig.setCreateDt(LocalDateTime.now());
            if (Objects.isNull(notifyConfigVO.getId())) {
                // insert
                doSaveNotifyConfig(groupConfigRequestVO.getGroupName(), notifyConfigVO);
            } else if (Objects.nonNull(notifyConfigVO.getId()) && notifyConfigVO.getIsDeleted() == 1) {
                // delete
                Assert.isTrue(1 == notifyConfigMapper.deleteById(notifyConfigVO.getId()),
                        new XRetryServerException("删除通知配置失败 sceneConfig:[{}]", JsonUtil.toJsonString(notifyConfigVO)));
            } else {
                // update
                Assert.isTrue(1 == notifyConfigMapper.update(notifyConfig,
                                new LambdaQueryWrapper<NotifyConfig>()
                                        .eq(NotifyConfig::getId, notifyConfigVO.getId())
                                        .eq(NotifyConfig::getGroupName, notifyConfig.getGroupName())
                                        .eq(NotifyConfig::getNotifyScene, notifyConfig.getNotifyScene())),
                        new XRetryServerException("更新通知配置失败 sceneConfig:[{}]", JsonUtil.toJsonString(notifyConfig)));
            }
        }
    }

}
