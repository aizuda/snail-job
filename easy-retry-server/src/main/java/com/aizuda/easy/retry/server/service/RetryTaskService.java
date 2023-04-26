package com.aizuda.easy.retry.server.service;

import com.aizuda.easy.retry.client.model.GenerateRetryBizIdDTO;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskRequestVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskSaveRequestVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskResponseVO;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
public interface RetryTaskService {

    PageResult<List<RetryTaskResponseVO>> getRetryTaskPage(RetryTaskQueryVO queryVO);

    RetryTaskResponseVO getRetryTaskById(String groupName, Long id);

    int updateRetryTaskStatus(RetryTaskRequestVO retryTaskRequestVO);

    /**
     * 手动新增重试任务
     *
     * @param retryTaskRequestVO {@link RetryTaskSaveRequestVO} 重试数据模型
     * @return
     */
    int saveRetryTask(RetryTaskSaveRequestVO retryTaskRequestVO);

    /**
     * 委托客户端生成bizId
     *
     * @param generateRetryBizIdDTO
     * @return
     */
    String bizIdGenerate(GenerateRetryBizIdDTO generateRetryBizIdDTO);
}
