package com.aizuda.snailjob.server.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.model.base.WorkflowTriggerRequest;
import com.aizuda.snailjob.model.request.WorkflowTriggerApiRequest;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.prepare.workflow.TerminalWorkflowPrepareHandler;
import com.aizuda.snailjob.server.service.convert.WorkflowTaskConverter;
import com.aizuda.snailjob.model.base.StatusUpdateRequest;
import com.aizuda.snailjob.model.base.JobTriggerRequest;
import com.aizuda.snailjob.server.service.kit.WorkflowKit;
import com.aizuda.snailjob.server.service.service.WorkflowService;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.JobSummary;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public abstract class AbstractWorkflowService implements WorkflowService {
    @Autowired
    protected WorkflowMapper workflowMapper;
    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    protected TerminalWorkflowPrepareHandler terminalWorkflowPrepareHandler;
    @Autowired
    protected JobSummaryMapper jobSummaryMapper;

    @Override
    public boolean updateWorkFlowStatus(StatusUpdateRequest requestDTO) {
        Workflow workflow = workflowMapper.selectById(requestDTO.getId());
        Assert.notNull(workflow, "workflow does not exist");

        // 直接幂等
        if (Objects.equals(requestDTO.getStatus(), workflow.getWorkflowStatus())) {
            return true;
        }

        Workflow updateWorkflow = new Workflow();
        if (Objects.equals(requestDTO.getStatus(), StatusEnum.YES.getStatus())) {
            // 开启时重新计算调度时间
            updateWorkflow.setNextTriggerAt(WorkflowKit.calculateNextTriggerAt(workflow.getTriggerType(), workflow.getTriggerInterval(), DateUtils.toNowMilli()));
        }

        updateWorkflow.setId(workflow.getId());
        updateWorkflow.setWorkflowStatus(requestDTO.getStatus());
        return 1 == workflowMapper.updateById(updateWorkflow);
    }

    @Override
    public boolean triggerWorkFlow(WorkflowTriggerRequest request) {
        Workflow workflow = workflowMapper.selectById(request.getWorkflowId());
        Assert.notNull(workflow, () -> new SnailJobServerException("workflow can not be null."));
        Assert.isTrue(workflow.getNamespaceId().equals(getNamespaceId()), () -> new SnailJobServerException("namespace id not match."));

        // 将字符串反序列化为 Set
        if (StrUtil.isNotBlank(workflow.getGroupName())) {
            Set<String> namesSet = new HashSet<>(Arrays.asList(workflow.getGroupName().split(", ")));

            // 判断任务节点相关组有无关闭，存在关闭组则停止执行工作流执行
            if (CollectionUtil.isNotEmpty(namesSet)) {
                for (String groupName : namesSet) {
                    long count = accessTemplate.getGroupConfigAccess().count(
                            new LambdaQueryWrapper<GroupConfig>()
                                    .eq(GroupConfig::getGroupName, groupName)
                                    .eq(GroupConfig::getNamespaceId, workflow.getNamespaceId())
                                    .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
                    );
                    Assert.isTrue(count > 0, () -> new SnailJobServerException("Group [{}] is closed, manual execution is not supported.", workflow.getGroupName()));
                }
            }
        }

        WorkflowTaskPrepareDTO prepareDTO = WorkflowTaskConverter.INSTANCE.toWorkflowTaskPrepareDTO(workflow);
        // 设置now表示立即执行
        prepareDTO.setNextTriggerAt(DateUtils.toNowMilli());
        prepareDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_WORKFLOW.getType());
        // 设置工作流上下文
        String tmpWfContext = request.getTmpWfContext();
        if (StrUtil.isNotBlank(tmpWfContext) && !JsonUtil.isEmptyJson(tmpWfContext)) {
            Map<String, Object> tmpWfContextMap = JsonUtil.parseHashMap(tmpWfContext);
            Map<String, Object> wfContextMap = JsonUtil.parseHashMap(workflow.getWfContext());
            tmpWfContextMap.putAll(wfContextMap);
            // 设置合并之后的上下文
            prepareDTO.setWfContext(JsonUtil.toJsonString(tmpWfContextMap));
        }
        terminalWorkflowPrepareHandler.handler(prepareDTO);
        return true;
    }

    @Override
    public boolean deleteWorkflowByIds(Set<Long> ids) {

        List<JobSummary> jobSummaries = jobSummaryMapper.selectList(new LambdaQueryWrapper<JobSummary>()
                .select(JobSummary::getId)
                .in(JobSummary::getBusinessId, ids)
                .eq(JobSummary::getNamespaceId, getNamespaceId())
                .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.WORKFLOW.getType())
        );
        if (CollUtil.isNotEmpty(jobSummaries)) {
            Assert.isTrue(jobSummaries.size() ==
                            jobSummaryMapper.deleteByIds(StreamUtils.toSet(jobSummaries, JobSummary::getId)),
                    () -> new SnailJobServerException("Summary table deletion failed")
            );
        }

        Assert.isTrue(ids.size() == workflowMapper.delete(
                new LambdaQueryWrapper<Workflow>()
                        .eq(Workflow::getNamespaceId, getNamespaceId())
                        .eq(Workflow::getWorkflowStatus, StatusEnum.NO.getStatus())
                        .in(Workflow::getId, ids)
        ), () -> new SnailJobServerException("Failed to delete workflow task, please check if the task status is closed"));

        return true;
    }


    protected abstract String getNamespaceId();

}
