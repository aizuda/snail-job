package com.aizuda.snailjob.template.datasource.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.aizuda.snailjob.template.datasource.exception.SnailJobDatasourceException;
import com.aizuda.snailjob.template.datasource.persistence.po.CreateDt;
import com.aizuda.snailjob.template.datasource.persistence.po.CreateUpdateDt;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * @author: dhb52 (adopted from ruoyi-vue-plus)
 * @date: 2024-06-16 23:22
 */
@Slf4j
public class InjectionMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof CreateDt baseEntity) {
                LocalDateTime current = ObjectUtil.isNotNull(baseEntity.getCreateDt())
                        ? baseEntity.getCreateDt() : LocalDateTime.now();
                baseEntity.setCreateDt(current);
            }
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof CreateUpdateDt baseEntity) {
                LocalDateTime current = ObjectUtil.isNotNull(baseEntity.getCreateDt())
                        ? baseEntity.getCreateDt() : LocalDateTime.now();
                baseEntity.setUpdateDt(current);
            }
        } catch (Exception e) {
            throw new SnailJobDatasourceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof CreateUpdateDt baseEntity) {
                LocalDateTime current = LocalDateTime.now();
                // 更新时间填充(不管为不为空)
                baseEntity.setUpdateDt(current);
            }
        } catch (Exception e) {
            throw new SnailJobDatasourceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

}
