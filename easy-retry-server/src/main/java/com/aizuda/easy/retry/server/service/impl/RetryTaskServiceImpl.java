package com.aizuda.easy.retry.server.service.impl;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.model.GenerateRetryBizIdDTO;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.config.RequestDataHelper;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.service.RetryTaskService;
import com.aizuda.easy.retry.server.service.convert.RetryTaskConverter;
import com.aizuda.easy.retry.server.service.convert.RetryTaskResponseVOConverter;
import com.aizuda.easy.retry.server.support.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.BatchDeleteRetryTaskVO;
import com.aizuda.easy.retry.server.web.model.request.GenerateRetryBizIdVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskUpdateStatusRequestVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskSaveRequestVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskUpdateExecutorNameRequestVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskResponseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
@Service
public class RetryTaskServiceImpl implements RetryTaskService {

    public static final String URL = "http://{0}:{1}/{2}/retry/generate/biz-id/v1";

    @Autowired
    private RetryTaskMapper retryTaskMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ClientNodeAllocateHandler clientNodeAllocateHandler;

    RetryTaskResponseVOConverter retryTaskResponseVOConverter = new RetryTaskResponseVOConverter();

    @Override
    public PageResult<List<RetryTaskResponseVO>> getRetryTaskPage(RetryTaskQueryVO queryVO) {

        PageDTO<RetryTask> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<RetryTask> retryTaskLambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(queryVO.getGroupName())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getGroupName, queryVO.getGroupName());
        } else {
            return new PageResult<>(pageDTO, new ArrayList<>());
        }

        if (StringUtils.isNotBlank(queryVO.getSceneName())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getSceneName, queryVO.getSceneName());
        }
        if (StringUtils.isNotBlank(queryVO.getBizNo())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getBizNo, queryVO.getBizNo());
        }
        if (StringUtils.isNotBlank(queryVO.getBizId())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getBizId, queryVO.getBizId());
        }
        if (Objects.nonNull(queryVO.getRetryStatus())) {
            retryTaskLambdaQueryWrapper.eq(RetryTask::getRetryStatus, queryVO.getRetryStatus());
        }

        RequestDataHelper.setPartition(queryVO.getGroupName());

        retryTaskLambdaQueryWrapper.select(RetryTask::getId, RetryTask::getBizNo, RetryTask::getBizId,
            RetryTask::getGroupName, RetryTask::getNextTriggerAt, RetryTask::getRetryCount,
            RetryTask::getRetryStatus, RetryTask::getUpdateDt, RetryTask::getSceneName);
        pageDTO = retryTaskMapper.selectPage(pageDTO, retryTaskLambdaQueryWrapper.orderByDesc(RetryTask::getCreateDt));
        return new PageResult<>(pageDTO, retryTaskResponseVOConverter.batchConvert(pageDTO.getRecords()));
    }

    @Override
    public RetryTaskResponseVO getRetryTaskById(String groupName, Long id) {
        RequestDataHelper.setPartition(groupName);
        RetryTask retryTask = retryTaskMapper.selectById(id);
        return retryTaskResponseVOConverter.convert(retryTask);
    }

    @Override
    public int updateRetryTaskStatus(RetryTaskUpdateStatusRequestVO retryTaskUpdateStatusRequestVO) {

        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(retryTaskUpdateStatusRequestVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new EasyRetryServerException("重试状态错误");
        }

        RequestDataHelper.setPartition(retryTaskUpdateStatusRequestVO.getGroupName());
        RetryTask retryTask = retryTaskMapper.selectById(retryTaskUpdateStatusRequestVO.getId());
        if (Objects.isNull(retryTask)) {
            throw new EasyRetryServerException("未查询到重试任务");
        }

        retryTask.setRetryStatus(retryTaskUpdateStatusRequestVO.getRetryStatus());
        retryTask.setGroupName(retryTaskUpdateStatusRequestVO.getGroupName());

        // 若恢复重试则需要重新计算下次触发时间
        if (RetryStatusEnum.RUNNING.getStatus().equals(retryStatusEnum.getStatus())) {
            retryTask.setNextTriggerAt(
                WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));
        }

        RequestDataHelper.setPartition(retryTaskUpdateStatusRequestVO.getGroupName());
        return retryTaskMapper.updateById(retryTask);
    }

    @Override
    public int saveRetryTask(final RetryTaskSaveRequestVO retryTaskRequestVO) {
        RetryStatusEnum retryStatusEnum = RetryStatusEnum.getByStatus(retryTaskRequestVO.getRetryStatus());
        if (Objects.isNull(retryStatusEnum)) {
            throw new EasyRetryServerException("重试状态错误");
        }

        RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryTaskRequestVO);
        retryTask.setCreateDt(LocalDateTime.now());
        retryTask.setUpdateDt(LocalDateTime.now());

        if (StringUtils.isBlank(retryTask.getExtAttrs())) {
            retryTask.setExtAttrs(StringUtils.EMPTY);
        }

        retryTask.setNextTriggerAt(
            WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));

        RequestDataHelper.setPartition(retryTaskRequestVO.getGroupName());
        return retryTaskMapper.insert(retryTask);
    }

    @Override
    public String bizIdGenerate(final GenerateRetryBizIdVO generateRetryBizIdVO) {
        ServerNode serverNode = clientNodeAllocateHandler.getServerNode(generateRetryBizIdVO.getGroupName());
        Assert.notNull(serverNode, () -> new EasyRetryServerException("生成bizId失败: 不存在活跃的客户端节点"));

        // 委托客户端生成bizId
        String url = MessageFormat
            .format(URL, serverNode.getHostIp(), serverNode.getHostPort().toString(), serverNode.getContextPath());

        GenerateRetryBizIdDTO generateRetryBizIdDTO = new GenerateRetryBizIdDTO();
        generateRetryBizIdDTO.setGroup(generateRetryBizIdVO.getGroupName());
        generateRetryBizIdDTO.setScene(generateRetryBizIdVO.getSceneName());
        generateRetryBizIdDTO.setArgsStr(generateRetryBizIdVO.getArgsStr());
        generateRetryBizIdDTO.setExecutorName(generateRetryBizIdVO.getExecutorName());

        HttpEntity<GenerateRetryBizIdDTO> requestEntity = new HttpEntity<>(generateRetryBizIdDTO);
        Result result = restTemplate.postForObject(url, requestEntity, Result.class);

        Assert.notNull(result, () -> new EasyRetryServerException("biz生成失败"));
        Assert.isTrue(1 == result.getStatus(), () -> new EasyRetryServerException("biz生成失败:请确保参数与执行器名称正确"));

        return (String) result.getData();
    }

    @Override
    public int updateRetryTaskExecutorName(final RetryTaskUpdateExecutorNameRequestVO requestVO) {

        RetryTask retryTask = new RetryTask();
        retryTask.setExecutorName(requestVO.getExecutorName());
        retryTask.setRetryStatus(requestVO.getRetryStatus());
        retryTask.setUpdateDt(LocalDateTime.now());

        // 根据重试数据id，更新执行器名称
        RequestDataHelper.setPartition(requestVO.getGroupName());
        return retryTaskMapper
            .update(retryTask, new LambdaUpdateWrapper<RetryTask>().in(RetryTask::getId, requestVO.getIds()));
    }

    @Override
    public Integer deleteRetryTask(final BatchDeleteRetryTaskVO requestVO) {
        RequestDataHelper.setPartition(requestVO.getGroupName());
        return retryTaskMapper.deleteBatchIds(requestVO.getIds());
    }
}
