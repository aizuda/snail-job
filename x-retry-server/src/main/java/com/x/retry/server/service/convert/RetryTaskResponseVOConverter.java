package com.x.retry.server.service.convert;

import com.x.retry.common.core.covert.AbstractConverter;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.web.model.response.RetryTaskResponseVO;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
public class RetryTaskResponseVOConverter extends AbstractConverter<RetryTask, RetryTaskResponseVO> {

    @Override
    public RetryTaskResponseVO convert(RetryTask retryTask) {
        RetryTaskResponseVO responseVO = convert(retryTask, RetryTaskResponseVO.class);
        return responseVO;
    }

    @Override
    public List<RetryTaskResponseVO> batchConvert(List<RetryTask> retryTasks) {
        return batchConvert(retryTasks, RetryTaskResponseVO.class);
    }
}
