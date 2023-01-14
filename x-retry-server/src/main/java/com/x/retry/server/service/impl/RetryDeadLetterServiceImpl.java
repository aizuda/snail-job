package com.x.retry.server.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.x.retry.common.core.util.Assert;
import com.x.retry.server.config.RequestDataHelper;
import com.x.retry.server.exception.XRetryServerException;
import com.x.retry.server.persistence.mybatis.mapper.RetryDeadLetterMapper;
import com.x.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.x.retry.server.persistence.mybatis.po.RetryDeadLetter;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.service.RetryDeadLetterService;
import com.x.retry.server.service.convert.RetryDeadLetterResponseVOConverter;
import com.x.retry.server.support.strategy.WaitStrategies;
import com.x.retry.server.web.model.base.PageResult;
import com.x.retry.server.web.model.request.RetryDeadLetterQueryVO;
import com.x.retry.server.web.model.response.RetryDeadLetterResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2022-02-28 09:46
 */
@Service
public class RetryDeadLetterServiceImpl implements RetryDeadLetterService {

    @Autowired
    private RetryDeadLetterMapper retryDeadLetterMapper;

    @Autowired
    private RetryTaskMapper retryTaskMapper;

    private RetryDeadLetterResponseVOConverter retryDeadLetterResponseVOConverter = new RetryDeadLetterResponseVOConverter();

    @Override
    public PageResult<List<RetryDeadLetterResponseVO>> getRetryDeadLetterPage(RetryDeadLetterQueryVO queryVO) {

        PageDTO<RetryDeadLetter> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        if (StringUtils.isBlank(queryVO.getGroupName()))  {
            return  new PageResult<>(pageDTO, new ArrayList<>());
        }

        RequestDataHelper.setPartition(queryVO.getGroupName());
        PageDTO<RetryDeadLetter> retryDeadLetterPageDTO = retryDeadLetterMapper.selectPage(pageDTO, null);

        return new PageResult<>(retryDeadLetterPageDTO,
                retryDeadLetterResponseVOConverter.batchConvert(retryDeadLetterPageDTO.getRecords()));
    }

    @Override
    public RetryDeadLetterResponseVO getRetryDeadLetterById(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);

        RetryDeadLetter retryDeadLetter = retryDeadLetterMapper.selectById(id);
        return retryDeadLetterResponseVOConverter.convert(retryDeadLetter);
    }

    @Override
    @Transactional
    public boolean rollback(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);
        RetryDeadLetter retryDeadLetter = retryDeadLetterMapper.selectById(id);

        RetryTask retryTask = new RetryTask();
        BeanUtils.copyProperties(retryDeadLetter, retryTask);
        retryTask.setNextTriggerAt(WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));
        retryTask.setCreateDt(LocalDateTime.now());
        retryTask.setUpdateDt(LocalDateTime.now());
        RequestDataHelper.setPartition(groupName);
        Assert.isTrue(1 == retryTaskMapper.insert(retryTask), new XRetryServerException("新增重试任务失败"));

        RequestDataHelper.setPartition(groupName);
        Assert.isTrue(1 == retryDeadLetterMapper.deleteById(retryDeadLetter.getId()), new XRetryServerException("删除死信队列数据失败"));

        return true;
    }

    @Override
    public boolean deleteById(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);
        return retryDeadLetterMapper.deleteById(id) == 1;
    }
}
