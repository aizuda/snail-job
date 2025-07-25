package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.RetryResponseVO;

import java.util.List;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
public interface RetryWebService {

    PageResult<List<RetryResponseVO>> getRetryPage(RetryQueryVO queryVO);

    /**
     * 手动新增重试任务
     *
     * @param retryTaskRequestVO {@link RetrySaveRequestVO} 重试数据模型
     * @return
     */
    int saveRetryTask(RetrySaveRequestVO retryTaskRequestVO);

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
    int updateRetryExecutorName(RetryUpdateExecutorNameRequestVO requestVO);

    /**
     * 批量删除重试数据
     *
     * @param requestVO 批量删除重试数据
     * @return
     */
    boolean batchDeleteRetry(BatchDeleteRetryTaskVO requestVO);

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

}
