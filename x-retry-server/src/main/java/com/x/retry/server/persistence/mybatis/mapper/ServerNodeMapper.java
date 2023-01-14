package com.x.retry.server.persistence.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ServerNodeMapper extends BaseMapper<ServerNode> {

    int insertOrUpdate(ServerNode record);

    int deleteByExpireAt(@Param("endTime") LocalDateTime endTime);

}