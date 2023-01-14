package com.x.retry.server.service.convert;

import com.x.retry.common.core.covert.AbstractConverter;
import com.x.retry.server.persistence.mybatis.po.RetryDeadLetter;
import com.x.retry.server.web.model.response.RetryDeadLetterResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 15:29
 */
public class RetryDeadLetterResponseVOConverter extends AbstractConverter<RetryDeadLetter, RetryDeadLetterResponseVO> {

    @Override
    public RetryDeadLetterResponseVO convert(RetryDeadLetter retryDeadLetter) {
        return convert(retryDeadLetter, RetryDeadLetterResponseVO.class);
    }

    @Override
    public List<RetryDeadLetterResponseVO> batchConvert(List<RetryDeadLetter> retryDeadLetters) {
        return batchConvert(retryDeadLetters, RetryDeadLetterResponseVO.class);
    }
}
