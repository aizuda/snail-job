package com.aizuda.snailjob.template.datasource.access;

import com.aizuda.snailjob.model.request.ConfigRequest;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

import java.util.List;
import java.util.Set;

/**
 * 获取配置通道
 *
 * @author: opensnail
 * @date : 2022-01-05 09:06
 */
public interface ConfigAccess<T> extends Access<T> {


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
     * @return {@link RetrySceneConfig} 场景配置
     */
    RetrySceneConfig getSceneConfigByGroupNameAndSceneName(String groupName, String sceneName, String namespaceId);

    /**
     * 获取场景配置集合
     *
     * @param groupNames   组名称
     * @param sceneNames   场景名称
     * @param namespaceIds 命名空间
     * @return {@link RetrySceneConfig} 场景配置
     */
    List<RetrySceneConfig> getSceneConfigByGroupNameAndSceneNameList(Set<String> groupNames, Set<String> sceneNames, Set<String> namespaceIds);

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
     * @param groupName 组名称
     * @return {@link RetrySceneConfig} 场景配置
     */
    List<RetrySceneConfig> getSceneConfigByGroupName(String groupName);

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
    List<RetrySceneConfig> getAllConfigSceneList();

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
    ConfigRequest getConfigInfo(String groupName, final String namespaceId);

    List<T> list(LambdaQueryWrapper<T> query);

    int update(T t, LambdaUpdateWrapper<T> query);

    int updateById(T t);

    int delete(LambdaQueryWrapper<T> query);

    int insert(T t);

    T one(LambdaQueryWrapper<T> query);

    PageDTO<T> listPage(PageDTO<T> iPage, LambdaQueryWrapper<T> query);

    long count(LambdaQueryWrapper<T> query);

}
