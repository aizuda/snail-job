package com.x.retry.server.persistence.support.processor;

import com.x.retry.server.persistence.mybatis.po.GroupConfig;
import com.x.retry.server.persistence.mybatis.po.NotifyConfig;
import com.x.retry.server.persistence.mybatis.po.SceneConfig;
import com.x.retry.server.persistence.support.ConfigAccess;
import com.x.retry.server.persistence.support.access.config.AbstractConfigAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public NotifyConfig getNotifyConfigByGroupName(String groupName, Integer notifyScene) {
        return configAccess.getNotifyConfigByGroupName(groupName, notifyScene);
    }

    @Override
    public List<NotifyConfig> getNotifyListConfigByGroupName(String groupName) {
        return configAccess.getNotifyListConfigByGroupName(groupName);
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

}
