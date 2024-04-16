package com.aizuda.snailjob.template.datasource.access.config;

import com.aizuda.snailjob.common.core.enums.NodeTypeEnum;
import com.aizuda.snailjob.common.core.enums.NotifySceneEnum;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.SceneConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.utils.DbUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取配置通道通用模板
 *
 * @author: opensnail
 * @date : 2022-01-05 09:12
 */
public abstract class AbstractConfigAccess<T> implements ConfigAccess<T> {

    @Autowired
    protected NotifyConfigMapper notifyConfigMapper;
    @Autowired
    protected SceneConfigMapper sceneConfigMapper;
    @Autowired
    protected GroupConfigMapper groupConfigMapper;

    protected static final List<String> ALLOW_DB = Arrays.asList(
        DbTypeEnum.MYSQL.getDb(),
        DbTypeEnum.MARIADB.getDb(),
        DbTypeEnum.POSTGRES.getDb(),
        DbTypeEnum.ORACLE.getDb(),
        DbTypeEnum.SQLSERVER.getDb());

    protected DbTypeEnum getDbType() {
        return DbUtils.getDbType();
    }

    protected List<NotifyConfig> getByGroupIdAndNotifyScene(String groupName, Integer notifyScene, String namespaceId) {
        return notifyConfigMapper.selectList(
            new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getNamespaceId, namespaceId)
                .eq(NotifyConfig::getGroupName, groupName)
                .eq(NotifyConfig::getNotifyScene, notifyScene));
    }

    private List<NotifyConfig> getByGroupIdAndSceneIdAndNotifyScene(String groupName, String sceneName,
        Integer notifyScene) {
        return notifyConfigMapper.selectList(
            new LambdaQueryWrapper<NotifyConfig>().eq(NotifyConfig::getGroupName, groupName)
                .eq(NotifyConfig::getBusinessId, sceneName)
                .eq(NotifyConfig::getNotifyScene, notifyScene));
    }

    protected RetrySceneConfig getByGroupNameAndSceneName(String groupName, String sceneName, String namespaceId) {
        return sceneConfigMapper.selectOne(new LambdaQueryWrapper<RetrySceneConfig>()
            .eq(RetrySceneConfig::getNamespaceId, namespaceId)
            .eq(RetrySceneConfig::getGroupName, groupName)
            .eq(RetrySceneConfig::getSceneName, sceneName));
    }

    protected List<RetrySceneConfig> getSceneConfigs(String groupName) {
        return sceneConfigMapper.selectList(new LambdaQueryWrapper<RetrySceneConfig>()
            .eq(RetrySceneConfig::getGroupName, groupName));
    }

    protected GroupConfig getByGroupName(String groupName, final String namespaceId) {
        return groupConfigMapper.selectOne(new LambdaQueryWrapper<GroupConfig>()
            .eq(GroupConfig::getNamespaceId, namespaceId)
            .eq(GroupConfig::getGroupName, groupName));
    }

    protected List<NotifyConfig> getNotifyConfigs(String groupName, String namespaceId) {
        return notifyConfigMapper.selectList(
            new LambdaQueryWrapper<NotifyConfig>()
                .eq(NotifyConfig::getNamespaceId, namespaceId)
                .eq(NotifyConfig::getGroupName, groupName));
    }

    @Override
    public Set<String> getGroupNameList(String namespaceId) {
        List<GroupConfig> groupList = getAllConfigGroupList(namespaceId);
        return groupList.stream().map(GroupConfig::getGroupName).collect(Collectors.toSet());
    }

    @Override
    public GroupConfig getGroupConfigByGroupName(String groupName, String namespaceId) {
        return getByGroupName(groupName, namespaceId);
    }

    @Override
    public RetrySceneConfig getSceneConfigByGroupNameAndSceneName(String groupName, String sceneName, String namespaceId) {
        return getByGroupNameAndSceneName(groupName, sceneName, namespaceId);
    }

    @Override
    public List<NotifyConfig> getNotifyConfigByGroupName(String groupName, Integer notifyScene, String namespaceId) {
        return getByGroupIdAndNotifyScene(groupName, notifyScene, namespaceId);
    }

    @Override
    public List<NotifyConfig> getNotifyConfigByGroupNameAndSceneName(String groupName, String sceneName,
        Integer notifyScene) {
        return getByGroupIdAndSceneIdAndNotifyScene(groupName, sceneName, notifyScene);
    }

    @Override
    public List<NotifyConfig> getNotifyListConfigByGroupName(String groupName, String namespaceId) {
        return getNotifyConfigs(groupName, namespaceId);
    }

    @Override
    public List<RetrySceneConfig> getSceneConfigByGroupName(String groupName) {
        return getSceneConfigs(groupName);
    }

    @Override
    public List<GroupConfig> getAllOpenGroupConfig(String namespaceId) {
        return getAllConfigGroupList(namespaceId).stream().filter(i -> StatusEnum.YES.getStatus().equals(i.getGroupStatus()))
            .collect(Collectors.toList());
    }

    @Override
    public Set<String> getBlacklist(String groupName, String namespaceId) {

        GroupConfig groupConfig = getByGroupName(groupName, namespaceId);
        if (Objects.isNull(groupConfig)) {
            return Collections.EMPTY_SET;
        }

        LambdaQueryWrapper<RetrySceneConfig> sceneConfigLambdaQueryWrapper = new LambdaQueryWrapper<RetrySceneConfig>()
            .select(RetrySceneConfig::getSceneName)
            .eq(RetrySceneConfig::getGroupName, groupName);

        if (StatusEnum.YES.getStatus().equals(groupConfig.getGroupStatus())) {
            sceneConfigLambdaQueryWrapper.eq(RetrySceneConfig::getSceneStatus, StatusEnum.NO.getStatus());
        }

        List<RetrySceneConfig> retrySceneConfigs = sceneConfigMapper.selectList(sceneConfigLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(retrySceneConfigs)) {
            return Collections.EMPTY_SET;
        }

        return retrySceneConfigs.stream().map(RetrySceneConfig::getSceneName).collect(Collectors.toSet());
    }

    @Override
    public List<GroupConfig> getAllConfigGroupList(String namespaceId) {
        List<GroupConfig> allSystemConfigGroupList = groupConfigMapper.selectList(
            new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getNamespaceId, namespaceId)
                .orderByAsc(GroupConfig::getId));
        if (CollectionUtils.isEmpty(allSystemConfigGroupList)) {
            return Collections.EMPTY_LIST;
        }

        return allSystemConfigGroupList;
    }

    @Override
    public List<RetrySceneConfig> getAllConfigSceneList() {
        List<RetrySceneConfig> allSystemConfigSceneList = sceneConfigMapper.selectList(
            new LambdaQueryWrapper<RetrySceneConfig>().orderByAsc(RetrySceneConfig::getId));
        if (CollectionUtils.isEmpty(allSystemConfigSceneList)) {
            return Collections.EMPTY_LIST;
        }
        return allSystemConfigSceneList;
    }

    @Override
    public Integer getConfigVersion(String groupName, String namespaceId) {
        GroupConfig groupConfig = getGroupConfigByGroupName(groupName, namespaceId);
        if (Objects.isNull(groupConfig)) {
            return 0;
        }

        return groupConfig.getVersion();
    }

    @Override
    public ConfigDTO getConfigInfo(String groupName, final String namespaceId) {

        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setSceneBlacklist(getBlacklist(groupName, namespaceId));
        configDTO.setVersion(getConfigVersion(groupName, namespaceId));

        List<NotifyConfig> notifyList = getNotifyListConfigByGroupName(groupName, namespaceId);

        List<ConfigDTO.Notify> notifies = new ArrayList<>();
        for (NotifyConfig notifyConfig : notifyList) {

            // 只选择客户端的通知配置即可
            NotifySceneEnum notifyScene = NotifySceneEnum.getNotifyScene(notifyConfig.getNotifyScene(),
                NodeTypeEnum.CLIENT);
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

        List<RetrySceneConfig> retrySceneConfig = getSceneConfigByGroupName(groupName);

        List<ConfigDTO.Scene> sceneList = new ArrayList<>();
        for (RetrySceneConfig config : retrySceneConfig) {
            ConfigDTO.Scene scene = new ConfigDTO.Scene();
            scene.setSceneName(config.getSceneName());
            scene.setDdl(config.getDeadlineRequest());
            sceneList.add(scene);
        }

        configDTO.setSceneList(sceneList);
        return configDTO;
    }
}
