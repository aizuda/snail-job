package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.vo.JobStatusUpdateRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * OPENAPI
 * 更新工作流状态
 */
@Component
@RequiredArgsConstructor
public class OpenApiUpdateWorkFlowStatusRequestHandler extends PostHttpRequestHandler {
    private final WorkflowMapper workflowMapper;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_UPDATE_WORKFLOW_STATUS.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        JobStatusUpdateRequestVO jobRequestVO = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), JobStatusUpdateRequestVO.class);
        Workflow workflow = workflowMapper.selectOne(
                new LambdaQueryWrapper<Workflow>()
                        .select(Workflow::getId)
                        .eq(Workflow::getId, jobRequestVO.getId()));

        if (Objects.isNull(workflow)){
            SnailJobLog.LOCAL.warn("工作流不存在");
            return new SnailJobRpcResult(false, retryRequest.getReqId());
        }
        workflow.setWorkflowStatus(jobRequestVO.getJobStatus());
        boolean update = 1 == workflowMapper.updateById(workflow);

        return new SnailJobRpcResult(update, retryRequest.getReqId());

    }
}
