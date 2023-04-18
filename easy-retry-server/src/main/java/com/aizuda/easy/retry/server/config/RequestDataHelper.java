package com.aizuda.easy.retry.server.config;

import com.aizuda.easy.retry.server.exception.XRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.GroupConfigMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 17:18
 */
public class RequestDataHelper {

    /**
     * 请求参数存取
     */
    private static final ThreadLocal<Map<String, Object>> REQUEST_DATA = new ThreadLocal<>();

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
        map.put("partition", partition);
        RequestDataHelper.setRequestData(map);

        REQUEST_DATA.set(map);
    }


    /**
     * 设置分区
     *
     * @param groupName 组名称
     */
    public static void setPartition(String groupName) {

        if (StringUtils.isBlank(groupName)) {
            throw new XRetryServerException("组名称不能为空");
        }

        GroupConfigMapper groupConfigMapper = SpringContext.getBeanByType(GroupConfigMapper.class);

        GroupConfig groupConfig = groupConfigMapper.selectOne(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getGroupName, groupName));
        if (Objects.isNull(groupConfig)) {
            throw new XRetryServerException("groupName:[{}]不存在", groupName);
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


    public static void remove() {
        REQUEST_DATA.remove();
    }

}
