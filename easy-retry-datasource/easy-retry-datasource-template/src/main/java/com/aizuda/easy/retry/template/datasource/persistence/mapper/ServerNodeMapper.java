package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ServerNodeMapper extends BaseMapper<ServerNode> {

    int insertOrUpdate(ServerNode record);

    int insertOrUpdate(List<ServerNode> records);

    int deleteByExpireAt(@Param("endTime") LocalDateTime endTime);

}
