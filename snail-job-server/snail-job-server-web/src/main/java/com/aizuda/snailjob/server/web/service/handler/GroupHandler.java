package com.aizuda.snailjob.server.web.service.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2024-05-31
 * @since : sj_1.0.0
 */
@Component
@RequiredArgsConstructor
public class GroupHandler {

    private final AccessTemplate accessTemplate;

    /**
     * 校验组是否存在
     *
     * @param groupNameSet 待校验的组
     * @param namespaceId  空间
     */
    public void validateGroupExistence(Set<String> groupNameSet, String namespaceId) {
        Assert.notEmpty(groupNameSet, () -> new SnailJobServerException("组不能为空"));
        List<GroupConfig> groupConfigs = accessTemplate.getGroupConfigAccess()
                .list(new LambdaQueryWrapper<GroupConfig>()
                        .select(GroupConfig::getGroupName)
                        .eq(GroupConfig::getNamespaceId, namespaceId)
                        .in(GroupConfig::getGroupName, groupNameSet)
                );

        Set<String> notExistedGroupNameSet = Sets.difference(groupNameSet,
                StreamUtils.toSet(groupConfigs, GroupConfig::getGroupName));

        Assert.isTrue(CollUtil.isEmpty(notExistedGroupNameSet),
                () -> new SnailJobServerException("组:{}不存在", notExistedGroupNameSet));
    }

}
