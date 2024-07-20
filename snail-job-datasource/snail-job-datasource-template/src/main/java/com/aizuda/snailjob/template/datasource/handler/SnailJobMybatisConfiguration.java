package com.aizuda.snailjob.template.datasource.handler;

import com.aizuda.snailjob.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetrySummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2024-07-19
 * @since : 1.2.0
 */
public class SnailJobMybatisConfiguration extends MybatisConfiguration {

    /**
     * 重复的ID不需要告警 配置列表
     * 此设计为了多数据源不同数据库包下面加载的xml有重复的mybatis id 会告警报错问题
     */
    static final Set<String> DUPLICATE_IDS = new HashSet<>();

    static {
        DUPLICATE_IDS.add(JobSummaryMapper.class.getName() + ".insertBatch");
        DUPLICATE_IDS.add(RetryTaskLogMapper.class.getName() + ".insertBatch");
        DUPLICATE_IDS.add(RetrySummaryMapper.class.getName() + ".insertBatch");
        DUPLICATE_IDS.add(RetryTaskLogMessageMapper.class.getName() + ".insertBatch");
        DUPLICATE_IDS.add(RetryTaskMapper.class.getName() + ".insertBatch");
        DUPLICATE_IDS.add(JobLogMessageMapper.class.getName() + ".insertBatch");
    }

    @Override
    public void addMappedStatement(final MappedStatement ms) {
        if (mappedStatements.containsKey(ms.getId())) {
            if (!DUPLICATE_IDS.contains(ms.getId())) {
                super.addMappedStatement(ms);
            }
        } else {
            super.addMappedStatement(ms);
        }
    }
}
