package com.x.retry.server.persistence.support.access.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.retry.common.core.enums.StatusEnum;
import com.x.retry.server.persistence.mybatis.po.GroupConfig;
import com.x.retry.server.persistence.mybatis.po.NotifyConfig;
import com.x.retry.server.persistence.mybatis.po.SceneConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据库存储
 *
 * @author: www.byteblogs.com
 * @date : 2022-01-05 09:05
 */
@Component
public class MybatisConfigAccess extends AbstractConfigAccess {

    @Override
    public Set<String> getGroupNameList() {
        List<GroupConfig> groupList = getAllConfigGroupList();
        return groupList.stream().map(GroupConfig::getGroupName).collect(Collectors.toSet());
    }

    @Override
    public GroupConfig getGroupConfigByGroupName(String shardingGroupId) {
      return getByGroupName(shardingGroupId);
    }

    @Override
    public SceneConfig getSceneConfigByGroupNameAndSceneName(String shardingGroupId, String sceneId) {
        return getByGroupNameAndSceneName(shardingGroupId, sceneId);
    }

    @Override
    public List<NotifyConfig> getNotifyConfigByGroupName(String shardingGroupId, Integer notifyScene) {
        return getByGroupIdAndNotifyScene(shardingGroupId, notifyScene);
    }

    @Override
    public List<NotifyConfig> getNotifyListConfigByGroupName(String shardingGroupId) {
        return getNotifyConfigs(shardingGroupId);
    }

    @Override
    public List<GroupConfig> getAllOpenGroupConfig() {
        return getAllConfigGroupList().stream().filter(i-> StatusEnum.YES.getStatus().equals(i.getGroupStatus())).collect(Collectors.toList());
    }

    @Override
    public Set<String> getBlacklist(String groupName) {

        GroupConfig groupConfig = getByGroupName(groupName);
        LambdaQueryWrapper<SceneConfig> sceneConfigLambdaQueryWrapper = new LambdaQueryWrapper<SceneConfig>()
          .eq(SceneConfig::getSceneName, groupName);

        if (StatusEnum.YES.getStatus().equals(groupConfig.getGroupStatus())) {
            sceneConfigLambdaQueryWrapper.eq(SceneConfig::getSceneStatus, StatusEnum.NO.getStatus());
        }

        List<SceneConfig> sceneConfigs = sceneConfigMapper.selectList(sceneConfigLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(sceneConfigs)) {
            return Collections.EMPTY_SET;
        }

        return sceneConfigs.stream().map(SceneConfig::getSceneName).collect(Collectors.toSet());
    }

    @Override
    public List<GroupConfig> getAllConfigGroupList() {
        List<GroupConfig> allSystemConfigGroupList = groupConfigMapper.selectList(new LambdaQueryWrapper<>());
        if (CollectionUtils.isEmpty(allSystemConfigGroupList)) {
            return Collections.EMPTY_LIST;
        }

        return allSystemConfigGroupList;
    }

}
