package com.aizuda.snailjob.template.datasource.access.job;

import com.aizuda.snailjob.template.datasource.access.JobAccess;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobExecutorMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobExecutor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.aizuda.snailjob.template.datasource.utils.DbUtils.getDbType;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.template.datasource.access.job
 * @Project：snail-job
 * @Date：2025/6/3 9:51
 * @Filename：JobExecutorAccess
 */
@Component
public class JobExecutorAccess implements JobAccess<JobExecutor> {

    @Autowired
    private JobExecutorMapper jobExecutorMapper;

    @Override
    public boolean supports(String operationType) {
        return DbTypeEnum.all().contains(getDbType()) && OperationTypeEnum.JOB_EXECUTORS.name().equals(operationType);
    }

    @Override
    public int insert(JobExecutor jobExecutor) {
        return jobExecutorMapper.insert(jobExecutor);
    }

    @Override
    public int insertBatch(List<JobExecutor> list) {
        return jobExecutorMapper.insertBatch(list);
    }

    @Override
    public PageDTO<JobExecutor> listPage(PageDTO<JobExecutor> queryDO, LambdaQueryWrapper<JobExecutor> query) {
        return jobExecutorMapper.selectPage(queryDO, query);
    }


    @Override
    public List<JobExecutor> list(LambdaQueryWrapper<JobExecutor> query) {
        return jobExecutorMapper.selectList(query);
    }

    @Override
    public JobExecutor one(LambdaQueryWrapper<JobExecutor> query) {
        return jobExecutorMapper.selectOne(query);
    }

    @Override
    public int update(JobExecutor jobExecutor, LambdaUpdateWrapper<JobExecutor> query) {
        return jobExecutorMapper.update(jobExecutor, query);
    }

    @Override
    public int updateById(JobExecutor jobExecutor) {
        return jobExecutorMapper.updateById(jobExecutor);
    }

    @Override
    public int delete(LambdaQueryWrapper<JobExecutor> query) {
        return jobExecutorMapper.delete(query);
    }

    @Override
    public long count(LambdaQueryWrapper<JobExecutor> query) {
        return jobExecutorMapper.selectCount(query);
    }
}
