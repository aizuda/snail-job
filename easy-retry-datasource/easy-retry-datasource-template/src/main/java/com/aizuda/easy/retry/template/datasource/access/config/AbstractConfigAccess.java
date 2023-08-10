package com.aizuda.easy.retry.template.datasource.access.config;

import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.template.datasource.access.ConfigAccess;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.NotifyConfigMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.SceneConfigMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取配置通道通用模板
 *
 * @author: www.byteblogs.com
 * @date : 2022-01-05 09:12
 */
public abstract class AbstractConfigAccess<T> implements ConfigAccess<T> {

    @Autowired
    protected NotifyConfigMapper notifyConfigMapper;
    @Autowired
    protected SceneConfigMapper sceneConfigMapper;
    @Autowired
    protected GroupConfigMapper groupConfigMapper;
    @Autowired
    protected Environment environment;
    protected static final List<String> ALLOW_DB =  Arrays.asList(DbTypeEnum.MYSQL.getDb(),
            DbTypeEnum.MARIADB.getDb(),
            DbTypeEnum.POSTGRES.getDb());

    protected DbTypeEnum getDbType() {
        String dbType = environment.getProperty("easy-retry.db-type");
        return DbTypeEnum.modeOf(dbType);
    }

    protected List<NotifyConfig> getByGroupIdAndNotifyScene(String groupName, Integer notifyScene) {
        return notifyConfigMapper.selectList(new LambdaQueryWrapper<NotifyConfig>().eq(NotifyConfig::getGroupName, groupName)
                .eq(NotifyConfig::getNotifyScene, notifyScene));
    }

    protected SceneConfig getByGroupNameAndSceneName(String groupName, String sceneName) {
        return sceneConfigMapper.selectOne(new LambdaQueryWrapper<SceneConfig>()
                .eq(SceneConfig::getGroupName, groupName).eq(SceneConfig::getSceneName, sceneName));
    }

    protected List<SceneConfig> getSceneConfigs(String groupName) {
        return sceneConfigMapper.selectList(new LambdaQueryWrapper<SceneConfig>()
            .eq(SceneConfig::getGroupName, groupName));
    }

    protected GroupConfig getByGroupName(String groupName) {
        return groupConfigMapper.selectOne(new LambdaQueryWrapper<GroupConfig>().eq(GroupConfig::getGroupName, groupName));
    }

    protected List<NotifyConfig> getNotifyConfigs(String groupName) {
        return notifyConfigMapper.selectList(new LambdaQueryWrapper<NotifyConfig>().eq(NotifyConfig::getGroupName, groupName));
    }

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
    public List<SceneConfig> getSceneConfigByGroupName(String groupName) {
        return getSceneConfigs(groupName);
    }

    @Override
    public List<GroupConfig> getAllOpenGroupConfig() {
        return getAllConfigGroupList().stream().filter(i-> StatusEnum.YES.getStatus().equals(i.getGroupStatus())).collect(Collectors.toList());
    }

    @Override
    public Set<String> getBlacklist(String groupName) {

        GroupConfig groupConfig = getByGroupName(groupName);
        if (Objects.isNull(groupConfig)) {
            return Collections.EMPTY_SET;
        }
        LambdaQueryWrapper<SceneConfig> sceneConfigLambdaQueryWrapper = new LambdaQueryWrapper<SceneConfig>()
                .eq(SceneConfig::getSceneName, groupName);

        if (Objects.isNull(groupConfig)) {
            return Collections.EMPTY_SET;
        }

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
        List<GroupConfig> allSystemConfigGroupList = groupConfigMapper.selectList(new LambdaQueryWrapper<GroupConfig>().orderByAsc(GroupConfig::getId));
        if (CollectionUtils.isEmpty(allSystemConfigGroupList)) {
            return Collections.EMPTY_LIST;
        }

        return allSystemConfigGroupList;
    }

    @Override
    public Integer getConfigVersion(String groupName) {
        GroupConfig groupConfig = getGroupConfigByGroupName(groupName);
        if (Objects.isNull(groupConfig)) {
            return 0;
        }

        return groupConfig.getVersion();
    }

    @Override
    public ConfigDTO getConfigInfo(String groupName) {

        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setSceneBlacklist(getBlacklist(groupName));
        configDTO.setVersion(getConfigVersion(groupName));

        List<NotifyConfig> notifyList = getNotifyListConfigByGroupName(groupName);

        List<ConfigDTO.Notify> notifies = new ArrayList<>();
        for (NotifyConfig notifyConfig : notifyList) {

            // 只选择客户端的通知配置即可
            NotifySceneEnum notifyScene = NotifySceneEnum.getNotifyScene(notifyConfig.getNotifyScene(), NodeTypeEnum.CLIENT);
            if (Objects.isNull(notifyScene)) {
                continue;
            }

            ConfigDTO.Notify notify = new ConfigDTO.Notify();
            notify.setNotifyScene(notifyConfig.getNotifyScene());
            notify.setNotifyType(notifyConfig.getNotifyType());
            notify.setNotifyThreshold(notifyConfig.getNotifyThreshold());
            notify.setNotifyAttribute(notifyConfig.getNotifyAttribute());
            notifies.add(notify);
        }

        configDTO.setNotifyList(notifies);

        List<SceneConfig> sceneConfig = getSceneConfigByGroupName(groupName);

        List<ConfigDTO.Scene> sceneList = new ArrayList<>();
        for (SceneConfig config : sceneConfig) {
            ConfigDTO.Scene scene = new ConfigDTO.Scene();
            scene.setSceneName(config.getSceneName());
            scene.setDdl(config.getDeadlineRequest());
            sceneList.add(scene);
        }

        configDTO.setSceneList(sceneList);
        return configDTO;
    }
}
