package com.aizuda.snailjob.template.datasource.persistence.mapper;

import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 工作流节点 Mapper 接口
 * </p>
 *
 * @author xiaowoniu
 * @since 2023-12-12
 */
@Mapper
public interface WorkflowNodeMapper extends BaseMapper<WorkflowNode> {

}
