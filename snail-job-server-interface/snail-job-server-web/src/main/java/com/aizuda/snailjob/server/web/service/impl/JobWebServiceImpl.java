package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.model.request.base.JobRequest;
import com.aizuda.snailjob.model.response.JobExistsResponse;
import com.aizuda.snailjob.model.response.base.JobResponse;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.service.service.impl.AbstractJobService;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.ExportJobVO;
import com.aizuda.snailjob.server.web.model.request.JobQueryVO;
import com.aizuda.snailjob.server.web.model.request.JobRequestWebVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.JobResponseWebVO;
import com.aizuda.snailjob.server.web.service.JobWebService;
import com.aizuda.snailjob.server.web.service.convert.JobConverter;
import com.aizuda.snailjob.server.web.service.convert.JobResponseVOConverter;
import com.aizuda.snailjob.server.web.service.handler.GroupHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.SystemUserMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.SystemUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author opensnail
 * @date 2023-10-11 22:20:42
 * @since 2.4.0
 */
@Service("jobWebCommonService")
@Slf4j
@RequiredArgsConstructor
@Validated
public class JobWebServiceImpl extends AbstractJobService implements JobWebService {

    private final JobMapper jobMapper;
    private final GroupHandler groupHandler;
    private final SystemUserMapper systemUserMapper;

    @Override
    public PageResult<List<JobResponseWebVO>> getJobPage(JobQueryVO queryVO) {

        PageDTO<Job> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        PageDTO<Job> selectPage = jobMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getNamespaceId, userSessionVO.getNamespaceId())
                        .in(CollUtil.isNotEmpty(groupNames), Job::getGroupName, groupNames)
                        .like(StrUtil.isNotBlank(queryVO.getJobName()), Job::getJobName, StrUtil.trim(queryVO.getJobName()))
                        .like(StrUtil.isNotBlank(queryVO.getExecutorInfo()), Job::getExecutorInfo, StrUtil.trim(queryVO.getExecutorInfo()))
                        .eq(Objects.nonNull(queryVO.getJobStatus()), Job::getJobStatus, queryVO.getJobStatus())
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .eq(Objects.nonNull(queryVO.getOwnerId()), Job::getOwnerId, queryVO.getOwnerId())
                        .orderByDesc(Job::getId));
        List<JobResponseWebVO> jobResponseList = JobResponseVOConverter.INSTANCE.convertList(selectPage.getRecords());

        for (JobResponseWebVO jobResponseWebVO : jobResponseList) {
            // 兼容Oracle OwnerId Null查询异常：java.sql.SQLException: 无效的列类型: 1111
            if (Objects.nonNull(jobResponseWebVO.getOwnerId()) && jobResponseWebVO.getOwnerId() > 0) {
                SystemUser systemUser = systemUserMapper.selectById(jobResponseWebVO.getOwnerId());
                jobResponseWebVO.setOwnerName(systemUser.getUsername());
            }
        }
        return new PageResult<>(pageDTO, jobResponseList);
    }

    @Override
    public List<String> getTimeByCron(String cron) {
        return CronUtils.getExecuteTimeByCron(cron, 5);
    }

    @Override
    public List<JobResponseWebVO> getJobNameList(String keywords, Long jobId, String groupName) {

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        PageDTO<Job> selectPage = jobMapper.selectPage(
                new PageDTO<>(1, 100),
                new LambdaQueryWrapper<Job>()
                        .select(Job::getId, Job::getJobName)
                        .eq(Job::getNamespaceId, userSessionVO.getNamespaceId())
                        .like(StrUtil.isNotBlank(keywords), Job::getJobName, StrUtil.trim(keywords))
                        .eq(StrUtil.isNotBlank(groupName), Job::getGroupName, groupName)
                        .eq(Objects.nonNull(jobId), Job::getId, jobId)
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        // SQLServer 分页必须 ORDER BY
                        .orderByDesc(Job::getId));
        return JobResponseVOConverter.INSTANCE.convertList(selectPage.getRecords());
    }


    @Override
    public List<JobResponseWebVO> getJobList(String groupName) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        List<Job> jobs = jobMapper.selectList(
                new LambdaQueryWrapper<Job>()
                        .select(Job::getId, Job::getJobName, Job::getExecutorInfo, Job::getTaskType, Job::getLabels)
                        .eq(Job::getNamespaceId, namespaceId)
                        .eq(Job::getGroupName, groupName)
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .orderByDesc(Job::getCreateDt));
        return JobResponseVOConverter.INSTANCE.convertList(jobs);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importJobs(List<JobRequestWebVO> requestList) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        groupHandler.validateGroupExistence(
                StreamUtils.toSet(requestList, JobRequestWebVO::getGroupName), namespaceId
        );
        for (JobRequestWebVO vo : requestList) {

            String bizId = vo.getBizId();
            // 兼容老版本 用id作为BizId
            if (StrUtil.isBlank(bizId)) {
                if (vo.getId() == null) {
                    addJob(vo);
                    continue;
                }
                bizId = String.valueOf(vo.getId());
            }
            vo.setBizId(bizId);

            JobExistsResponse existsResponse = existsJobByBizId(bizId);
            if (existsResponse == null) {
                addJob(vo);
                continue;
            }
            vo.setId(existsResponse.getId());
            updateJob(vo);
        }
    }

    @Override
    public String exportJobs(ExportJobVO exportJobVO) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<JobRequestWebVO> requestList = new ArrayList<>();
        PartitionTaskUtils.process(startId -> {
                    List<Job> jobList = jobMapper.selectPage(new PageDTO<>(0, 100, Boolean.FALSE),
                            new LambdaQueryWrapper<Job>()
                                    .eq(Job::getNamespaceId, namespaceId)
                                    .eq(StrUtil.isNotBlank(exportJobVO.getGroupName()), Job::getGroupName, exportJobVO.getGroupName())
                                    .likeRight(StrUtil.isNotBlank(exportJobVO.getJobName()), Job::getJobName, StrUtil.trim(exportJobVO.getJobName()))
                                    .eq(Objects.nonNull(exportJobVO.getJobStatus()), Job::getJobStatus, exportJobVO.getJobStatus())
                                    .in(CollUtil.isNotEmpty(exportJobVO.getJobIds()), Job::getId, exportJobVO.getJobIds())
                                    .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                                    .gt(Job::getId, startId)
                                    .orderByAsc(Job::getId)
                    ).getRecords();
                    return StreamUtils.toList(jobList, JobPartitionTask::new);
                },
                partitionTasks -> {
                    List<JobPartitionTask> jobPartitionTasks = (List<JobPartitionTask>) partitionTasks;
                    requestList.addAll(
                            JobConverter.INSTANCE.convertList(StreamUtils.toList(jobPartitionTasks, JobPartitionTask::getJob)));
                }, 0);

        return JsonUtil.toJsonString(requestList);
    }

    @Override
    protected void getJobByIdAfter(JobResponse responseBaseDTO, Job job) {
        JobResponseWebVO jobResponseWebVO = (JobResponseWebVO) responseBaseDTO;
        SystemUser systemUser = systemUserMapper.selectById(job.getOwnerId());
        if (Objects.nonNull(systemUser)) {
            jobResponseWebVO.setOwnerName(systemUser.getUsername());
        }
        jobResponseWebVO.setOwnerId(job.getOwnerId());
    }

    @Override
    protected void updateJobPreValidator(JobRequest jobRequest) {

    }

    @Override
    protected String getNamespaceId() {
        return UserSessionUtils.currentUserSession().getNamespaceId();
    }

    @Override
    protected void addJobPopulate(Job job, JobRequest request) {

    }

    @Override
    protected void addJobPreValidator(JobRequest request) {

    }

    @EqualsAndHashCode(callSuper = true)
    @Getter
    private static class JobPartitionTask extends PartitionTask {

        // 这里就直接放GroupConfig为了后面若加字段不需要再这里在调整了
        private final Job job;

        public JobPartitionTask(Job job) {
            this.job = job;
            setId(job.getId());
        }
    }

}
