package com.aizuda.snailjob.server.job.task.support.generator.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.utils.DbUtils;
import com.google.common.collect.Sets;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-02 13:08:14
 * @since 2.4.0
 */
public abstract class AbstractJobTaskGenerator implements JobTaskGenerator, InitializingBean {

    @Autowired
    private JobTaskMapper jobTaskMapper;

    @Override
    public List<JobTask> generate(JobTaskGenerateContext context) {
        return doGenerate(context);
    }

    protected abstract List<JobTask> doGenerate(JobTaskGenerateContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        JobTaskGeneratorFactory.registerTaskInstance(getTaskInstanceType(), this);
    }

    protected void batchSaveJobTasks(List<JobTask> jobTasks) {
        // ORACLE 批次插入不能直接返回id，因此此处特殊处理
        // 后期版本会对snail-job-datasource进行重构，在考虑此处的兼容逻辑
        if (Sets.newHashSet(DbTypeEnum.ORACLE.getDb(), DbTypeEnum.SQLSERVER.getDb())
                .contains(DbUtils.getDbType().getDb())) {
            // sqlserver oracle 不支持返回批量id,故暂时先这样处理
            for (JobTask jobTask : jobTasks) {
                Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new SnailJobServerException("Adding new task instance failed"));
            }
        } else {
            Assert.isTrue(jobTasks.size() == jobTaskMapper.insertBatch(jobTasks), () -> new SnailJobServerException("Adding new task instance failed"));
        }
    }
}
