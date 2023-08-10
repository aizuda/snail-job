package com.aizuda.easy.retry.template.datasource.access;

import com.aizuda.easy.retry.template.datasource.access.config.GroupConfigAccess;
import com.aizuda.easy.retry.template.datasource.access.config.NotifyConfigAccess;
import com.aizuda.easy.retry.template.datasource.access.config.SceneConfigAccess;
import com.aizuda.easy.retry.template.datasource.access.task.RetryDeadLetterTaskAccess;
import com.aizuda.easy.retry.template.datasource.access.task.RetryTaskAccess;
import com.aizuda.easy.retry.template.datasource.enums.OperationTypeEnum;
import com.aizuda.easy.retry.template.datasource.exception.EasyRetryDatasourceException;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据处理模板类
 *
 * @author www.byteblogs.com
 * @date 2023-08-06 09:55:12
 * @since 2.2.0
 */
@Component
public class AccessTemplate {

    @Autowired
    private List<Access> accesses;

    /**
     * 获取重试任务操作类
     *
     * @return {@link RetryTaskAccess} 重试任务操作类
     */
    public TaskAccess<RetryTask> getRetryTaskAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.RETRY_TASK.name())) {
                return (TaskAccess<RetryTask>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

    /**
     * 获取死信任务操作类
     *
     * @return {@link RetryDeadLetterTaskAccess} 获取死信任务操作类
     */
    public TaskAccess<RetryDeadLetter> getRetryDeadLetterAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.RETRY_DEAD_LETTER.name())) {
                return (TaskAccess<RetryDeadLetter>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

    /**
     * 获取场景配置操作类
     *
     * @return {@link SceneConfigAccess} 获取场景配置操作类
     */
    public ConfigAccess<SceneConfig> getSceneConfigAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.SCENE.name())) {
                return (ConfigAccess<SceneConfig>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

    /**
     * 获取组配置操作类
     *
     * @return {@link GroupConfigAccess} 获取组配置操作类
     */
    public ConfigAccess<GroupConfig> getGroupConfigAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.GROUP.name())) {
                return (ConfigAccess<GroupConfig>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

    /**
     * 获取通知配置操作类
     *
     * @return {@link NotifyConfigAccess} 获取通知配置操作类
     */
    public ConfigAccess<NotifyConfig> getNotifyConfigAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.GROUP.name())) {
                return (ConfigAccess<NotifyConfig>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

}
