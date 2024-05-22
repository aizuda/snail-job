package com.aizuda.snailjob.common.core.util;

import com.aizuda.snailjob.common.core.context.SpringContext;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * 获取环境信息
 *
 * @author: opensnail
 * @date : 2021-12-01 14:27
 */
public class EnvironmentUtils {
    public static final String DEFAULT_ENV = "default ";

    /**
     * 获取日志状态
     *
     * @return
     */
    public static Boolean getLogStatus() {

        Environment environment = SpringContext.getBean(Environment.class);
        if (Objects.nonNull(environment)) {
            return environment.getProperty("snail.job.log.status", Boolean.class, Boolean.TRUE);
        }

        return Boolean.TRUE;
    }

    /**
     * 获取环境
     *
     * @return DEV、FAT、UAT、PROD
     */
    public static String getActiveProfile() {

        Environment environment = SpringContext.getBean(Environment.class);
        if (Objects.isNull(environment)) {
            return DEFAULT_ENV;
        }

        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return DEFAULT_ENV;
        }

        StringBuilder envs = new StringBuilder();
        for (String activeProfile : activeProfiles) {
            envs.append(activeProfile).append(" ");
        }
        return envs.toString();
    }

}
