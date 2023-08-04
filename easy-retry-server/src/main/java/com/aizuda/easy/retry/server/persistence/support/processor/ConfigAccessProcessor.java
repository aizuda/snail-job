package com.aizuda.easy.retry.server.persistence.support.processor;

import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO.Scene;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.persistence.support.access.config.AbstractConfigAccess;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 获取配置代理类
 *
 * @author: www.byteblogs.com
 * @date : 2022-01-05 10:03
 */
@Component
public class ConfigAccessProcessor implements ConfigAccess {

    @Autowired
    @Qualifier("mybatisConfigAccess")
    private AbstractConfigAccess configAccess;

    @Override
    public Set<String> getGroupNameList() {
        return configAccess.getGroupNameList();
    }

    @Override
    public GroupConfig getGroupConfigByGroupName(String groupName) {
        return configAccess.getGroupConfigByGroupName(groupName);
    }

    @Override
    public SceneConfig getSceneConfigByGroupNameAndSceneName(String groupName, String sceneName) {
        return configAccess.getSceneConfigByGroupNameAndSceneName(groupName, sceneName);
    }

    @Override
    public List<NotifyConfig> getNotifyConfigByGroupName(String groupName, Integer notifyScene) {
        return configAccess.getNotifyConfigByGroupName(groupName, notifyScene);
    }

    @Override
    public List<NotifyConfig> getNotifyListConfigByGroupName(String groupName) {
        return configAccess.getNotifyListConfigByGroupName(groupName);
    }

    @Override
    public List<SceneConfig> getSceneConfigByGroupName(final String groupName) {
        return configAccess.getSceneConfigByGroupName(groupName);
    }

    @Override
    public List<GroupConfig> getAllOpenGroupConfig() {
        return configAccess.getAllOpenGroupConfig();
    }

    @Override
    public Set<String> getBlacklist(String groupName) {
        return configAccess.getBlacklist(groupName);
    }

    @Override
    public List<GroupConfig> getAllConfigGroupList() {
        return configAccess.getAllConfigGroupList();
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

        List<Scene> sceneList = new ArrayList<>();
        for (SceneConfig config : sceneConfig) {
            Scene scene = new Scene();
            scene.setSceneName(config.getSceneName());
            scene.setDdl(config.getDeadlineRequest());
            sceneList.add(scene);
        }

        configDTO.setSceneList(sceneList);
        return configDTO;
    }


}
