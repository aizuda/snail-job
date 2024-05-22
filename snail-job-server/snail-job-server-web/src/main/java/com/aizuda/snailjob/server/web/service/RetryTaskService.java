package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.RetryTaskResponseVO;

import java.util.List;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
public interface RetryTaskService {

    PageResult<List<RetryTaskResponseVO>> getRetryTaskPage(RetryTaskQueryVO queryVO);

    /**
     * 通过重试任务表id获取重试任务信息
     *
     * @param groupName 组名称
     * @param id        重试任务表id
     * @return 重试任务
     */
    RetryTaskResponseVO getRetryTaskById(String groupName, Long id);

    /**
     * 更新重试任务状态
     *
     * @param retryTaskUpdateStatusRequestVO 更新重试任务状态请求模型
     * @return
     */
    int updateRetryTaskStatus(RetryTaskUpdateStatusRequestVO retryTaskUpdateStatusRequestVO);

    /**
     * 手动新增重试任务
     *
     * @param retryTaskRequestVO {@link RetryTaskSaveRequestVO} 重试数据模型
     * @return
     */
    int saveRetryTask(RetryTaskSaveRequestVO retryTaskRequestVO);

    /**
     * 委托客户端生成idempotentId
     *
     * @param generateRetryIdempotentIdVO 生成idempotentId请求模型
     * @return
     */
    String idempotentIdGenerate(GenerateRetryIdempotentIdVO generateRetryIdempotentIdVO);

    /**
     * 若客户端在变更了执行器,从而会导致执行重试任务时找不到执行器类，因此使用者可以在后端进行执行变更
     *
     * @param requestVO 更新执行器变更模型
     * @return 更新条数
     */
    int updateRetryTaskExecutorName(RetryTaskUpdateExecutorNameRequestVO requestVO);

    /**
     * 批量删除重试数据
     *
     * @param requestVO 批量删除重试数据
     * @return
     */
    Integer deleteRetryTask(BatchDeleteRetryTaskVO requestVO);

    /**
     * 解析日志
     *
     * @param parseLogsVO {@link ParseLogsVO} 解析参数模型
     * @return
     */
    Integer parseLogs(ParseLogsVO parseLogsVO);

    /**
     * 手动支持重试任务
     *
     * @param requestVO
     * @return
     */
    boolean manualTriggerRetryTask(ManualTriggerTaskRequestVO requestVO);

    /**
     * 手动执行回调任务
     *
     * @param requestVO
     * @return
     */
    boolean manualTriggerCallbackTask(ManualTriggerTaskRequestVO requestVO);
}
