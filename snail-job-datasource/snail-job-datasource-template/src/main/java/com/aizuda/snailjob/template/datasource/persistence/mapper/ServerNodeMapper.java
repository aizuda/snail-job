package com.aizuda.snailjob.template.datasource.persistence.mapper;

import com.aizuda.snailjob.template.datasource.persistence.dataobject.ActivePodQuantityResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServerNodeMapper extends BaseMapper<ServerNode> {

    int updateBatchExpireAt(@Param("list") List<ServerNode> list);

    int insertBatch(@Param("records") List<ServerNode> records);

    List<ActivePodQuantityResponseDO> selectActivePodCount(@Param("ew") Wrapper<ServerNode> wrapper);

}
