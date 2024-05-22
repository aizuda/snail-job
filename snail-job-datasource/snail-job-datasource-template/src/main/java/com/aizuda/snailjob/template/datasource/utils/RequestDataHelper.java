package com.aizuda.snailjob.template.datasource.utils;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.template.datasource.exception.SnailJobDatasourceException;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 组分区处理类
 *
 * @author: opensnail
 * @date : 2022-02-28 17:18
 * @since 1.0.0
 */
public class RequestDataHelper {

    /**
     * 请求参数存取
     */
    private static final ThreadLocal<Map<String, Object>> REQUEST_DATA = new ThreadLocal<>();
    private static final String PARTITION = "group-partition";

    /**
     * 设置请求参数
     *
     * @param requestData 请求参数 MAP 对象
     */
    public static void setRequestData(Map<String, Object> requestData) {
        REQUEST_DATA.set(requestData);
    }

    /**
     * 设置分区
     *
     * @param partition
     */
    public static void setPartition(int partition) {

        Map<String, Object> map = new HashMap<>();
        map.put(PARTITION, partition);
        RequestDataHelper.setRequestData(map);

    }


    /**
     * 设置分区
     *
     * @param groupName 组名称
     */
    public static void setPartition(String groupName, String namespaceId) {

        if (StrUtil.isBlank(groupName) && StrUtil.isNotBlank(namespaceId)) {
            throw new SnailJobDatasourceException("组名称或者命名空间ID不能为空");
        }

        GroupConfigMapper groupConfigMapper = SpringContext.getBeanByType(GroupConfigMapper.class);

        GroupConfig groupConfig = groupConfigMapper.selectOne(
                new LambdaQueryWrapper<GroupConfig>()
                        .select(GroupConfig::getGroupPartition)
                        .eq(GroupConfig::getNamespaceId, namespaceId)
                        .eq(GroupConfig::getGroupName, groupName));
        if (Objects.isNull(groupConfig)) {
            throw new SnailJobDatasourceException("groupName:[{}]不存在", groupName);
        }

        setPartition(groupConfig.getGroupPartition());
    }

    /**
     * 获取请求参数
     *
     * @param param 请求参数
     * @return 请求参数 MAP 对象
     */
    public static <T> T getRequestData(String param) {
        Map<String, Object> dataMap = getRequestData();
        if (CollectionUtils.isNotEmpty(dataMap)) {
            return (T) dataMap.get(param);
        }
        return null;
    }

    /**
     * 获取请求参数
     *
     * @return 请求参数 MAP 对象
     */
    public static Map<String, Object> getRequestData() {
        return REQUEST_DATA.get();
    }

    public static Integer getPartition() {
        Map<String, Object> requestData = getRequestData();
        if (CollectionUtils.isEmpty(requestData)) {
            return null;
        }

        return (Integer) requestData.get(PARTITION);
    }


    public static void remove() {
        REQUEST_DATA.remove();
    }

}
