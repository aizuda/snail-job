package com.aizuda.snail.job.template.datasource.persistence.mapper;

import com.aizuda.snail.job.template.datasource.persistence.dataobject.ActivePodQuantityResponseDO;
import com.aizuda.snail.job.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServerNodeMapper extends BaseMapper<ServerNode> {

    int batchUpdateExpireAt(@Param("list") List<ServerNode> list);

    int batchInsert(@Param("records") List<ServerNode> records);

    List<ActivePodQuantityResponseDO> countActivePod(@Param("ew") Wrapper<ServerNode> wrapper);

}
