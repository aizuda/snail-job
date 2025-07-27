package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.model.request.CallbackConfig;
import com.aizuda.snailjob.model.request.DecisionConfigRequest;
import com.aizuda.snailjob.server.common.convert.JobBatchResponseVOConverter;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.vo.JobBatchResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowNode;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * OPENAPI
 * 获取定时任务批次详情
 * @since 1.5.0
 */

@Component
@RequiredArgsConstructor
@Deprecated
public class OpenApiGetJobBatchDetailRequestHandler extends PostHttpRequestHandler {
    private final JobMapper jobMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final WorkflowNodeMapper workflowNodeMapper;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_GET_JOB_BATCH_DETAIL.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("query job batch content:[{}]", content);
        SnailJobRequest jobRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = jobRequest.getArgs();
        Long jobBatchId = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), Long.class);
        Assert.notNull(jobBatchId, () -> new SnailJobServerException("id cannot be null"));



        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(jobBatchId);
        if (Objects.isNull(jobTaskBatch)) {
            return new SnailJobRpcResult(null, jobRequest.getReqId());
        }

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        JobBatchResponseVO jobBatchResponseVO = JobBatchResponseVOConverter.INSTANCE.convert(jobTaskBatch, job);

        if (jobTaskBatch.getSystemTaskType().equals(SyetemTaskTypeEnum.WORKFLOW.getType())) {
            WorkflowNode workflowNode = workflowNodeMapper.selectById(jobTaskBatch.getWorkflowNodeId());
            jobBatchResponseVO.setNodeName(workflowNode.getNodeName());

            // 回调节点
            if (SystemConstants.CALLBACK_JOB_ID.equals(jobTaskBatch.getJobId())) {
                jobBatchResponseVO.setCallback(JsonUtil.parseObject(workflowNode.getNodeInfo(), CallbackConfig.class));
                jobBatchResponseVO.setExecutionAt(jobTaskBatch.getCreateDt());
                return new SnailJobRpcResult(jobBatchResponseVO, jobRequest.getReqId());
            }

            // 条件节点
            if (SystemConstants.DECISION_JOB_ID.equals(jobTaskBatch.getJobId())) {
                jobBatchResponseVO.setDecision(JsonUtil.parseObject(workflowNode.getNodeInfo(), DecisionConfigRequest.class));
                jobBatchResponseVO.setExecutionAt(jobTaskBatch.getCreateDt());
                return new SnailJobRpcResult(jobBatchResponseVO, jobRequest.getReqId());
            }
        }

        return new SnailJobRpcResult(jobBatchResponseVO, jobRequest.getReqId());
    }

}
