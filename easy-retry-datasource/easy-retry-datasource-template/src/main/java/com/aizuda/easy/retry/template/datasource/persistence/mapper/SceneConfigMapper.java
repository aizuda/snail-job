package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@InterceptorIgnore(tenantLine = "false")
public interface SceneConfigMapper extends BaseMapper<SceneConfig> {

}
