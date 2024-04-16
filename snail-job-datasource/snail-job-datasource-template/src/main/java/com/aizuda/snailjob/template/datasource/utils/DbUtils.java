package com.aizuda.snailjob.template.datasource.utils;

import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.common.core.context.SpringContext;
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
