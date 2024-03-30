package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.dataobject.ActivePodQuantityResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ServerNodeMapper extends BaseMapper<ServerNode> {

//    int insertOrUpdate(@Param("records") List<ServerNode> records);

    int batchUpdateExpireAt(@Param("list") List<ServerNode> list);

    int batchInsert(@Param("records") List<ServerNode> records);

    List<ActivePodQuantityResponseDO> countActivePod(@Param("ew") Wrapper<ServerNode> wrapper);

}
