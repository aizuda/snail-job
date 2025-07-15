package com.aizuda.snailjob.server.service.service.impl;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.CallbackConfig;
import com.aizuda.snailjob.server.common.dto.DecisionConfig;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.service.convert.JobBatchResponseConverter;
import com.aizuda.snailjob.server.service.dto.JobBatchResponseBaseDTO;
import com.aizuda.snailjob.server.service.service.JobBatchService;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public abstract class AbstractJobBatchService implements JobBatchService {
    @Autowired
    protected JobTaskBatchMapper jobTaskBatchMapper;
    @Autowired
    protected JobMapper jobMapper;
    @Autowired
    protected WorkflowNodeMapper workflowNodeMapper;

    @Override
    public JobBatchResponseBaseDTO getJobBatchById(Long jobBatchId) {
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(jobBatchId);
        if (Objects.isNull(jobTaskBatch)) {
            return null;
        }

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        JobBatchResponseBaseDTO jobBatchResponse = JobBatchResponseConverter.INSTANCE.convert(jobTaskBatch, job);

        if (jobTaskBatch.getSystemTaskType().equals(SyetemTaskTypeEnum.WORKFLOW.getType())) {
            WorkflowNode workflowNode = workflowNodeMapper.selectById(jobTaskBatch.getWorkflowNodeId());
            jobBatchResponse.setNodeName(workflowNode.getNodeName());

            // 回调节点
            if (SystemConstants.CALLBACK_JOB_ID.equals(jobTaskBatch.getJobId())) {
                jobBatchResponse.setCallback(JsonUtil.parseObject(workflowNode.getNodeInfo(), CallbackConfig.class));
                jobBatchResponse.setExecutionAt(jobTaskBatch.getCreateDt());
                return jobBatchResponse;
            }

            // 条件节点
            if (SystemConstants.DECISION_JOB_ID.equals(jobTaskBatch.getJobId())) {
                jobBatchResponse.setDecision(JsonUtil.parseObject(workflowNode.getNodeInfo(), DecisionConfig.class));
                jobBatchResponse.setExecutionAt(jobTaskBatch.getCreateDt());
                return jobBatchResponse;
            }
        }

        return jobBatchResponse;
    }
}
