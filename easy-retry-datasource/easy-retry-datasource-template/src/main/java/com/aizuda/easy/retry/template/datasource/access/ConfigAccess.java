package com.aizuda.easy.retry.template.datasource.access;

import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

import java.util.List;
import java.util.Set;

/**
 * 获取配置通道
 *
 * @author: www.byteblogs.com
 * @date : 2022-01-05 09:06
 */
public interface ConfigAccess<T> extends Access<T>  {

    /**
     * 获取所有组id列表
     *
     * @return 组id列表
     */
    Set<String> getGroupNameList(String namespaceId);

    /**
     * 根据组id获取缓存上下文
     *
     * @param groupName 组名称
     */
    GroupConfig getGroupConfigByGroupName(String groupName, String namespaceId);

    /**
     * 获取场景配置
     *
     * @param groupName   组名称
     * @param sceneName   场景名称
     * @param namespaceId 命名空间
     * @return {@link SceneConfig} 场景配置
     */
    SceneConfig getSceneConfigByGroupNameAndSceneName(String groupName, String sceneName, String namespaceId);

    /**
     * 获取通知配置
     *
     * @param groupName     组名称
     * @param notifyScene {@link NotifySceneEnum} 场景类型
     * @return {@link NotifyConfig} 场景配置
     */
    List<NotifyConfig> getNotifyConfigByGroupName(String groupName,Integer notifyScene, String namespaceId);


    /**
     * 获取通知配置
     *
     * @param groupName     组名称
     * @param groupName     场景名称
     * @param notifyScene {@link NotifySceneEnum} 场景类型
     * @return {@link NotifyConfig} 场景配置
     */
    List<NotifyConfig> getNotifyConfigByGroupNameAndSceneName(String groupName, String sceneName, Integer notifyScene);

    /**
     * 获取通知配置
     *
     * @param shardingGroupId 组名称
     * @param namespaceId
     * @return {@link NotifyConfig} 通知配置
     */
    List<NotifyConfig> getNotifyListConfigByGroupName(String shardingGroupId, String namespaceId);

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
    List<GroupConfig> getAllOpenGroupConfig(String namespaceId);

    /**
     * 场景黑名单
     *
     * @return 黑名单列表
     */
    Set<String> getBlacklist(String groupName, String namespaceId);

    /**
     * 获取所有组配置信息
     *
     * @return 组配置列表
     */
    List<GroupConfig> getAllConfigGroupList(String namespaceId);


    /**
     * 获取所有场景配置信息
     *
     * @return 场景配置列表
     */
    List<SceneConfig> getAllConfigSceneList();

    /**
     * 获取配置版本号
     *
     * @param groupName   组名称
     * @param namespaceId
     * @return 版本号
     */
    Integer getConfigVersion(String groupName, final String namespaceId);

    /**
     * 同步客户端配置
     *
     * @param groupName   组名称
     * @param namespaceId 命名空间id
     * @return ConfigDTO
     */
    ConfigDTO getConfigInfo(String groupName, final String namespaceId);

    List<T> list(LambdaQueryWrapper<T> query);

    int update(T t, LambdaUpdateWrapper<T> query);

    int updateById(T t);

    int delete(LambdaQueryWrapper<T> query);

    int insert(T t);

    T one(LambdaQueryWrapper<T> query);

    PageDTO<T> listPage(PageDTO<T> iPage, LambdaQueryWrapper<T> query);

    long count(LambdaQueryWrapper<T> query);

}
