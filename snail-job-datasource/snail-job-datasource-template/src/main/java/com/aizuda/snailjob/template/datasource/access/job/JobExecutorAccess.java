package com.aizuda.snailjob.template.datasource.access.job;

import com.aizuda.snailjob.template.datasource.access.JobAccess;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.enums.OperationTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobExecutorsMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobExecutors;
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
public class JobExecutorAccess implements JobAccess<JobExecutors> {

    @Autowired
    private JobExecutorsMapper jobExecutorsMapper;

    @Override
    public boolean supports(String operationType) {
        return DbTypeEnum.all().contains(getDbType()) && OperationTypeEnum.JOB_EXECUTORS.name().equals(operationType);
    }

    @Override
    public int insert(JobExecutors jobExecutors) {
        return jobExecutorsMapper.insert(jobExecutors);
    }

    @Override
    public int insertBatch(List<JobExecutors> list) {
        return jobExecutorsMapper.insertBatch(list);
    }

    @Override
    public PageDTO<JobExecutors> listPage(PageDTO<JobExecutors> queryDO, LambdaQueryWrapper<JobExecutors> query) {
        return jobExecutorsMapper.selectPage(queryDO, query);
    }


    @Override
    public List<JobExecutors> list(LambdaQueryWrapper<JobExecutors> query) {
        return jobExecutorsMapper.selectList(query);
    }

    @Override
    public JobExecutors one(LambdaQueryWrapper<JobExecutors> query) {
        return jobExecutorsMapper.selectOne(query);
    }

    @Override
    public int update(JobExecutors jobExecutors, LambdaUpdateWrapper<JobExecutors> query) {
        return jobExecutorsMapper.update(jobExecutors, query);
    }

    @Override
    public int updateById(JobExecutors jobExecutors) {
        return jobExecutorsMapper.updateById(jobExecutors);
    }

    @Override
    public int delete(LambdaQueryWrapper<JobExecutors> query) {
        return jobExecutorsMapper.delete(query);
    }

    @Override
    public long count(LambdaQueryWrapper<JobExecutors> query) {
        return jobExecutorsMapper.selectCount(query);
    }
}
