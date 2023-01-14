package com.x.retry.server.persistence.support;

import com.x.retry.common.core.enums.NotifySceneEnum;
import com.x.retry.server.model.dto.ConfigDTO;
import com.x.retry.server.persistence.mybatis.po.GroupConfig;
import com.x.retry.server.persistence.mybatis.po.NotifyConfig;
import com.x.retry.server.persistence.mybatis.po.SceneConfig;

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
    NotifyConfig getNotifyConfigByGroupName(String groupName, Integer notifyScene);

    /**
     * 获取通知配置
     *
     * @param groupName     组名称
     * @return {@link NotifyConfig} 场景配置
     */
    List<NotifyConfig> getNotifyListConfigByGroupName(String groupName);

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
    Set<String> getBlacklist(String shardingGroupId);

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
