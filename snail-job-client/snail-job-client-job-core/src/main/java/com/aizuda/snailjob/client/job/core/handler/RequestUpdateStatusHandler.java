package com.aizuda.snailjob.client.job.core.handler;

import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.RequestUpdateStatusDTO;
import com.aizuda.snailjob.client.job.core.enums.JobTypeEnum;
import com.aizuda.snailjob.client.job.core.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.StatusEnum;


public class RequestUpdateStatusHandler extends AbstractRequestHandler<Boolean>{
    private final RequestUpdateStatusDTO statusDTO;
    // 1: job; 2: workflow
    private final int type;

    public RequestUpdateStatusHandler(Long id, int type) {
        this.statusDTO = new RequestUpdateStatusDTO();
        this.type = type;
        setId(id);
    }

    @Override
    protected Boolean doExecute() {
        if (type == JobTypeEnum.JOB.getType()){
            return (Boolean) client.updateJobStatus(statusDTO).getData();
        }
        if (type == JobTypeEnum.WORKFLOW.getType()){
            return (Boolean) client.updateWorkFlowStatus(statusDTO).getData();
        }
        throw new SnailJobClientException("snail job openapi check error");
    }

    @Override
    protected boolean checkRequest() {
        return  ValidatorUtils.validateEntity(statusDTO);
    }

    /**
     * 设置任务/工作流ID
     * @param id
     * @return
     */
    private RequestUpdateStatusHandler setId(Long id){
        this.statusDTO.setId(id);
        return this;
    }

    /**
     * 设置状态
     * @param status
     * @return
     */
    public RequestUpdateStatusHandler setStatus(StatusEnum status){
        this.statusDTO.setJobStatus(status.getStatus());
        return this;
    }
}
