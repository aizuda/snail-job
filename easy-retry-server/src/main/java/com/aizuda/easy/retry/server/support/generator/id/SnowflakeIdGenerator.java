package com.aizuda.easy.retry.server.support.generator.id;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.aizuda.easy.retry.server.enums.IdGeneratorMode;
import com.aizuda.easy.retry.server.support.generator.IdGenerator;
import org.springframework.stereotype.Component;

/**
 * 使用hutool自带的雪花算法生成id
 * 若出现时间回拨问题则直接报错 {@link Snowflake#tilNextMillis(long)}
 *
 * @author www.byteblogs.com
 * @date 2023-05-04
 * @since 1.2.0
 */
@Component
public class SnowflakeIdGenerator implements IdGenerator {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    @Override
    public boolean supports(int mode) {
        return IdGeneratorMode.SNOWFLAKE.getMode() == mode;
    }

    @Override
    public String idGenerator(String group) {
        return SNOWFLAKE.nextIdStr();
    }
}
