package com.aizuda.snailjob.template.datasource.access.config;

import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.NodeTypeEnum;
import com.aizuda.snailjob.common.core.enums.RetryNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.server.model.dto.ConfigDTO.Notify;
import com.aizuda.snailjob.server.model.dto.ConfigDTO.Notify.Recipient;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NotifyRecipientMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.SceneConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.NotifyRecipient;
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
    @Autowired
    protected NotifyRecipientMapper notifyRecipientMapper;

    protected static final List<String> ALLOW_DB = Arrays.asList(
            DbTypeEnum.MYSQL.getDb(),
            DbTypeEnum.MARIADB.getDb(),
            DbTypeEnum.POSTGRES.getDb(),
            DbTypeEnum.ORACLE.getDb(),
            DbTypeEnum.SQLSERVER.getDb());

    protected DbTypeEnum getDbType() {
        return DbUtils.getDbType();
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
                        .eq(NotifyConfig::getGroupName, groupName)
                        .eq(NotifyConfig::getNotifyStatus, StatusEnum.YES.getStatus())
        );
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
    public List<NotifyConfig> getNotifyListConfigByGroupName(String groupName, String namespaceId) {
        return getNotifyConfigs(groupName, namespaceId);
    }

    @Override
    public List<RetrySceneConfig> getSceneConfigByGroupName(String groupName) {
        return getSceneConfigs(groupName);
    }


    @Override
    public Set<String> getBlacklist(String groupName, String namespaceId) {

        GroupConfig groupConfig = getByGroupName(groupName, namespaceId);
        if (Objects.isNull(groupConfig)) {
            return new HashSet<>();
        }

        LambdaQueryWrapper<RetrySceneConfig> sceneConfigLambdaQueryWrapper = new LambdaQueryWrapper<RetrySceneConfig>()
                .select(RetrySceneConfig::getSceneName)
                .eq(RetrySceneConfig::getGroupName, groupName);

        if (StatusEnum.YES.getStatus().equals(groupConfig.getGroupStatus())) {
            sceneConfigLambdaQueryWrapper.eq(RetrySceneConfig::getSceneStatus, StatusEnum.NO.getStatus());
        }

        List<RetrySceneConfig> retrySceneConfigs = sceneConfigMapper.selectList(sceneConfigLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(retrySceneConfigs)) {
            return new HashSet<>();
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
            return new ArrayList<>();
        }

        return allSystemConfigGroupList;
    }

    @Override
    public List<RetrySceneConfig> getAllConfigSceneList() {
        List<RetrySceneConfig> allSystemConfigSceneList = sceneConfigMapper.selectList(
                new LambdaQueryWrapper<RetrySceneConfig>().orderByAsc(RetrySceneConfig::getId));
        if (CollectionUtils.isEmpty(allSystemConfigSceneList)) {
            return new ArrayList<>();
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
        configDTO.setVersion(getConfigVersion(groupName, namespaceId));

        List<NotifyConfig> notifyList = getNotifyListConfigByGroupName(groupName, namespaceId);

        List<ConfigDTO.Notify> notifies = new ArrayList<>();
        for (NotifyConfig notifyConfig : notifyList) {

            // 只选择客户端的通知配置即可
            RetryNotifySceneEnum retryNotifyScene = RetryNotifySceneEnum.getNotifyScene(notifyConfig.getNotifyScene(),
                    NodeTypeEnum.CLIENT);
            JobNotifySceneEnum jobNotifyScene = JobNotifySceneEnum.getJobNotifyScene(notifyConfig.getNotifyScene(),
                    NodeTypeEnum.CLIENT);
            if (Objects.isNull(retryNotifyScene) && Objects.isNull(jobNotifyScene)) {
                continue;
            }

            String recipientIds = notifyConfig.getRecipientIds();
            List<NotifyRecipient> notifyRecipients = notifyRecipientMapper.selectBatchIds(
                    JsonUtil.parseList(recipientIds, Long.class));
            notifies.add(getNotify(notifyConfig, notifyRecipients, retryNotifyScene, jobNotifyScene));
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

    private static Notify getNotify(final NotifyConfig notifyConfig, final List<NotifyRecipient> notifyRecipients,
                                    final RetryNotifySceneEnum retryNotifyScene, final JobNotifySceneEnum jobNotifyScene) {
        List<Recipient> recipients = new ArrayList<>();
        for (final NotifyRecipient notifyRecipient : notifyRecipients) {
            Recipient recipient = new Recipient();
            recipient.setNotifyAttribute(notifyRecipient.getNotifyAttribute());
            recipient.setNotifyType(notifyRecipient.getNotifyType());
            recipients.add(recipient);
        }

        Notify notify = new Notify();
        if (Objects.nonNull(retryNotifyScene)) {
            notify.setRetryNotifyScene(retryNotifyScene.getNotifyScene());
        }

        if (Objects.nonNull(jobNotifyScene)) {
            notify.setJobNotifyScene(jobNotifyScene.getNotifyScene());
        }

        notify.setNotifyThreshold(notifyConfig.getNotifyThreshold());
        notify.setRecipients(recipients);
        return notify;
    }
}
