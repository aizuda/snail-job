package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.po.SequenceAlloc;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 号段模式序号ID分配表 Mapper 接口
 * </p>
 *
 * @author www.byteblogs.com
 * @date 2023-05-05
 * @since 1.2.0
 */
@Mapper
public interface SequenceAllocMapper extends BaseMapper<SequenceAlloc> {

    /**
     * 更新业务类型下的最大id
     *
     * @param step      步长
     * @param groupName
     * @return 更新结果
     */
    Integer updateMaxIdByCustomStep(@Param("step") Integer step, @Param("groupName") String groupName, @Param("namespaceId") String namespaceId);

    /**
     * 更新最大id
     *
     * @param groupName 组名称
     * @return 更新结果
     */
    Integer updateMaxId(@Param("groupName") String groupName, @Param("namespaceId") String namespaceId);
}
