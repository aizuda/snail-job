package com.aizuda.easy.retry.template.datasource.utils;

import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import org.springframework.core.env.Environment;

/**
 * 数据库工具
 *
 * @author: 疯狂的狮子Li
 * @date : 2024-03-27 14:17
 */
public class DbUtils {

    public static DbTypeEnum getDbType() {
        Environment environment = SpringContext.getBean(Environment.class);
        String url = environment.getProperty("spring.datasource.url");
        return DbTypeEnum.modeOf(url);
    }

}
