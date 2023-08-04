package com.aizuda.easy.retry.server.persistence.support;

import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;

import java.util.List;
import java.util.Set;

/**
 * 获取配置通道
 *
 * @author: www.byteblogs.com
 * @date : 2022-01-05 09:06
 */
public interface ConfigAccess {

    /**
     * 获取所有组id列表
     *
     * @return 组id列表
     */
    Set<String> getGroupNameList();

    /**
     * 根据组id获取缓存上下文
     *
     * @param groupName 组名称
     */
    GroupConfig getGroupConfigByGroupName(String groupName);

    /**
     * 获取场景配置
     *
     * @param groupName 组名称
     * @param sceneName 场景名称
     * @return {@link SceneConfig} 场景配置
     */
    SceneConfig getSceneConfigByGroupNameAndSceneName(String groupName, String sceneName);

    /**
     * 获取通知配置
     *
     * @param groupName     组名称
     * @param notifyScene {@link NotifySceneEnum} 场景类型
     * @return {@link NotifyConfig} 场景配置
     */
    List<NotifyConfig> getNotifyConfigByGroupName(String groupName, Integer notifyScene);

    /**
     * 获取通知配置
     *
     * @param groupName     组名称
     * @return {@link NotifyConfig} 通知配置
     */
    List<NotifyConfig> getNotifyListConfigByGroupName(String groupName);

    /**
     * 获取场景配置
     *
     * @param groupName     组名称
     * @return {@link SceneConfig} 场景配置
     */
    List<SceneConfig> getSceneConfigByGroupName(String groupName);

    /**
     * 获取已开启的组配置信息
     *
     */
    List<GroupConfig> getAllOpenGroupConfig();

    /**
     * 场景黑名单
     *
     * @return 黑名单列表
     */
    Set<String> getBlacklist(String groupName);

    /**
     * 获取所有组配置信息
     *
     * @return 组配置列表
     */
    List<GroupConfig> getAllConfigGroupList();

    /**
     * 获取配置版本号
     *
     * @param groupName 组名称
     * @return 版本号
     */
    Integer getConfigVersion(String groupName);

    /**
     *
     * @param groupName 组名称
     * @return ConfigDTO
     */
    ConfigDTO getConfigInfo(String groupName);

}
