package com.aizuda.easy.retry.template.datasource.access;

import com.aizuda.easy.retry.template.datasource.enums.OperationTypeEnum;
import com.aizuda.easy.retry.template.datasource.exception.EasyRetryDatasourceException;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-08-06 09:55:12
 * @since 2.2.0
 */
@Component
public class AccessTemplate {

    @Autowired
    private List<Access> accesses;

    public TaskAccess<RetryTask> getRetryTaskAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.RETRY_TASK.name())) {
                return (TaskAccess<RetryTask>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

    public TaskAccess<RetryDeadLetter> getRetryDeadLetterAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.RETRY_DEAD_LETTER.name())) {
                return (TaskAccess<RetryDeadLetter>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

    public ConfigAccess<SceneConfig> getSceneConfigAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.SCENE.name())) {
                return (ConfigAccess<SceneConfig>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

    public ConfigAccess<GroupConfig> getGroupConfigAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.GROUP.name())) {
                return (ConfigAccess<GroupConfig>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

    public ConfigAccess<NotifyConfig> getNotifyConfigAccess() {

        for (Access access : accesses) {
            if (access.supports(OperationTypeEnum.GROUP.name())) {
                return (ConfigAccess<NotifyConfig>) access;
            }
        }

        throw new EasyRetryDatasourceException("not supports operation type");
    }

}
