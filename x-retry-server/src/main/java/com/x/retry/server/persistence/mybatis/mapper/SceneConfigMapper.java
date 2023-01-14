package com.x.retry.server.persistence.mybatis.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.retry.server.persistence.mybatis.po.SceneConfig;

@InterceptorIgnore(tenantLine = "false")
public interface SceneConfigMapper extends BaseMapper<SceneConfig> {

}
