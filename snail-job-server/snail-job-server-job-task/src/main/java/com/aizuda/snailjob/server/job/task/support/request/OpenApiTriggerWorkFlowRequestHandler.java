package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.JobTriggerDTO;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.WorkflowPrePareHandler;
import com.aizuda.snailjob.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Workflow;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * OPENAPI
 * 新增工作流任务
 */
@Component
@RequiredArgsConstructor
public class OpenApiTriggerWorkFlowRequestHandler extends PostHttpRequestHandler {
    private final WorkflowMapper workflowMapper;
    private final AccessTemplate accessTemplate;
    private final WorkflowPrePareHandler terminalWorkflowPrepareHandler;

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_TRIGGER_WORKFLOW.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SnailJobLog.LOCAL.debug("Trigger job content:[{}]", content);
        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);
        Object[] args = retryRequest.getArgs();
        JobTriggerDTO workflowDTO = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), JobTriggerDTO.class);
        Workflow workflow = workflowMapper.selectById(workflowDTO.getJobId());
        Assert.notNull(workflow, () -> new SnailJobServerException("workflow can not be null."));

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

                    if (count <= 0){
                        SnailJobLog.LOCAL.warn("组:[{}]已经关闭，不支持手动执行.", workflow.getGroupName());
                        return new SnailJobRpcResult(false, retryRequest.getReqId());
                    }
                }
            }
        }

        WorkflowTaskPrepareDTO prepareDTO = WorkflowTaskConverter.INSTANCE.toWorkflowTaskPrepareDTO(workflow);
        // 设置now表示立即执行
        prepareDTO.setNextTriggerAt(DateUtils.toNowMilli());
        prepareDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_WORKFLOW.getType());
//        设置工作流上下文
        String tmpWfContext = workflowDTO.getTmpArgsStr();
        if (StrUtil.isNotBlank(tmpWfContext) && !JsonUtil.isEmptyJson(tmpWfContext)){
            prepareDTO.setWfContext(tmpWfContext);
        }
        terminalWorkflowPrepareHandler.handler(prepareDTO);

        return new SnailJobRpcResult(true, retryRequest.getReqId());

    }
}
