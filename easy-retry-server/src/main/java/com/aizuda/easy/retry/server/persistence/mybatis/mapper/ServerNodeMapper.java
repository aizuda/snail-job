package com.aizuda.easy.retry.server.persistence.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface ServerNodeMapper extends BaseMapper<ServerNode> {

    int insertOrUpdate(ServerNode record);

    int deleteByExpireAt(@Param("endTime") LocalDateTime endTime);

}