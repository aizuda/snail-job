package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RetryOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.retry.task.dto.TaskStopJobDTO;
import com.aizuda.snailjob.server.retry.task.support.handler.RetryTaskStopHandler;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.event.WsSendEvent;
import com.aizuda.snailjob.server.web.model.request.RetryArgsDeserializeVO;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.snailjob.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.RetryResponseVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskResponseVO;
import com.aizuda.snailjob.server.web.service.RetryService;
import com.aizuda.snailjob.server.web.service.RetryTaskService;
import com.aizuda.snailjob.server.retry.task.convert.RetryConverter;
import com.aizuda.snailjob.server.web.service.convert.RetryTaskLogResponseVOConverter;
import com.aizuda.snailjob.server.web.service.convert.RetryTaskResponseVOConverter;
import com.aizuda.snailjob.server.web.timer.LogTimerWheel;
import com.aizuda.snailjob.server.web.timer.RetryTaskLogTimerTask;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.common.PageResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.RetryTaskLogMessageDO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.log.RetryTaskLogMessageQueryDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author: opensnail
 * @date : 2022-02-28 09:10
 */
@Service
@RequiredArgsConstructor
public class RetryTaskServiceImpl implements RetryTaskService {
    private static final Long DELAY_MILLS = 5000L;
    private final RetryTaskMapper retryTaskMapper;
    private final RetryMapper retryMapper;
    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    private final RetryTaskStopHandler retryTaskStopHandler;
    private final AccessTemplate accessTemplate;
    private final RetryService retryService;

    @Override
    public PageResult<List<RetryTaskResponseVO>> getRetryTaskLogPage(RetryTaskQueryVO queryVO) {
        PageDTO<RetryTask> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();

        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        LambdaQueryWrapper<RetryTask> wrapper = new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getNamespaceId, namespaceId)
                .in(CollUtil.isNotEmpty(groupNames), RetryTask::getGroupName, groupNames)
                .eq(StrUtil.isNotBlank(queryVO.getSceneName()), RetryTask::getSceneName, queryVO.getSceneName())
                .eq(queryVO.getTaskStatus() != null, RetryTask::getTaskStatus, queryVO.getTaskStatus())
                .eq(Objects.nonNull(queryVO.getRetryId()), RetryTask::getRetryId, queryVO.getRetryId())
                .between(ObjUtil.isNotNull(queryVO.getDatetimeRange()),
                        RetryTask::getCreateDt, queryVO.getStartDt(), queryVO.getEndDt())
                .select(RetryTask::getGroupName, RetryTask::getId, RetryTask::getSceneName, RetryTask::getTaskStatus,
                        RetryTask::getCreateDt, RetryTask::getUpdateDt, RetryTask::getTaskType, RetryTask::getOperationReason, RetryTask::getRetryId)
                .orderByDesc(RetryTask::getCreateDt);

        PageDTO<RetryTask> retryTaskPageDTO = retryTaskMapper.selectPage(pageDTO, wrapper);
        return new PageResult<>(retryTaskPageDTO,
                RetryTaskLogResponseVOConverter.INSTANCE.convertList(retryTaskPageDTO.getRecords()));

    }

    @Override
    public void getRetryTaskLogMessagePage(RetryTaskLogMessageQueryVO queryVO) {
        String sid = queryVO.getSid();
        RetryTaskLogMessageQueryDO pageQueryDO = new RetryTaskLogMessageQueryDO();
        pageQueryDO.setPage(1);
        pageQueryDO.setSize(50);
        pageQueryDO.setRetryTaskId(queryVO.getRetryTaskId());
        pageQueryDO.setStartRealTime(queryVO.getStartRealTime());
        pageQueryDO.setSearchCount(true);
        // 拉取数据
        PageResponseDO<RetryTaskLogMessageDO> pageResponseDO = accessTemplate.getRetryTaskLogMessageAccess()
                .listPage(pageQueryDO);

        long total = pageResponseDO.getTotal();

        int totalPage = (int) ((total + queryVO.getSize() - 1) / queryVO.getSize());

        Long lastRealTime = 0L;

        if (0 == totalPage &&
                (null != pageQueryDO.getStartRealTime() && 0 != pageQueryDO.getStartRealTime())){
            lastRealTime = pageQueryDO.getStartRealTime();
        }

        for (int i = 1; i <= totalPage;) {
            for (RetryTaskLogMessageDO retryTaskLogMessageDO : pageResponseDO.getRows()) {
                // 发生日志内容到前端
                String message = retryTaskLogMessageDO.getMessage();
                List<Map<String, String>> logContents = JsonUtil.parseObject(message, List.class);
                logContents = logContents.stream()
                        .sorted(Comparator.comparingLong(o -> Long.parseLong(o.get(LogFieldConstants.TIME_STAMP))))
                        .toList();
                for (Map<String, String> logContent : logContents) {
                    // send发消息
                    WsSendEvent sendEvent = new WsSendEvent(this);
                    sendEvent.setSid(sid);
                    sendEvent.setMessage(JsonUtil.toJsonString(logContent));
                    SnailSpringContext.getContext().publishEvent(sendEvent);
                }
            }

            // 继续查询下一页
            pageQueryDO.setSearchCount(false);
            pageQueryDO.setPage(++i);
            pageResponseDO = accessTemplate.getRetryTaskLogMessageAccess()
                    .listPage(pageQueryDO);
        }

        RetryTask retryTask = retryTaskMapper.selectOne(
                new LambdaQueryWrapper<RetryTask>().eq(RetryTask::getId, queryVO.getRetryTaskId()));

        if (Objects.isNull(retryTask)
                || (RetryTaskStatusEnum.TERMINAL_STATUS_SET.contains(retryTask.getTaskStatus()) &&
                retryTask.getUpdateDt().plusSeconds(15).isBefore(LocalDateTime.now()))) {
            // 发生完成标识
            WsSendEvent sendEvent = new WsSendEvent(this);
            sendEvent.setMessage("END");
            sendEvent.setSid(sid);
            SnailSpringContext.getContext().publishEvent(sendEvent);
        } else {
            // 覆盖作为下次查询的起始条件
            queryVO.setStartRealTime(lastRealTime);
            scheduleNextAttempt(queryVO, sid);
        }
    }

    /**
     * 使用时间轮5秒再进行日志查询
     *
     * @param queryVO
     * @param sid
     */
    private void scheduleNextAttempt(RetryTaskLogMessageQueryVO queryVO, String sid) {
        LogTimerWheel.registerWithJobTaskLog(() -> new RetryTaskLogTimerTask(queryVO, sid), Duration.ofMillis(DELAY_MILLS));
    }

    @Override
    public RetryTaskResponseVO getRetryTaskById(Long id) {
        RetryTask retryTask = retryTaskMapper.selectById(id);

        if (retryTask == null) {
            return null;
        }

        Retry retry = retryMapper.selectById(retryTask.getRetryId());
        RetryTaskResponseVO responseVO = RetryTaskLogResponseVOConverter.INSTANCE.convert(retryTask);

        RetryResponseVO retryResponseVO = RetryTaskResponseVOConverter.INSTANCE.convert(retry);
        RetryArgsDeserializeVO retryArgsDeserializeVO = new RetryArgsDeserializeVO();
        retryArgsDeserializeVO.setArgsStr(retryResponseVO.getArgsStr());
        retryArgsDeserializeVO.setExecutorName(retryResponseVO.getExecutorName());
        retryArgsDeserializeVO.setSceneName(retryResponseVO.getSceneName());
        retryArgsDeserializeVO.setGroupName(retryResponseVO.getGroupName());
        retryArgsDeserializeVO.setSerializerName(retryResponseVO.getSerializerName());
        retryResponseVO.setArgsStr(JsonUtil.toJsonString(retryService.deserialize(retryArgsDeserializeVO)));

        responseVO.setResponseVO(retryResponseVO);
        return responseVO;
    }

    @Override
    @Transactional
    public boolean deleteById(final Long id) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        RetryTask retryTask = retryTaskMapper.selectOne(
                new LambdaQueryWrapper<RetryTask>()
                        .in(RetryTask::getTaskStatus, RetryTaskStatusEnum.TERMINAL_STATUS_SET)
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .eq(RetryTask::getId, id));
        Assert.notNull(retryTask, () -> new SnailJobServerException("Data deletion failed"));

        retryTaskLogMessageMapper.delete(new LambdaQueryWrapper<RetryTaskLogMessage>()
                .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                .eq(RetryTaskLogMessage::getGroupName, retryTask.getGroupName())
                .eq(RetryTaskLogMessage::getRetryTaskId, id)
        );

        return 1 == retryTaskMapper.deleteById(id);
    }

    @Override
    @Transactional
    public boolean batchDelete(final Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<RetryTask> retryTasks = retryTaskMapper.selectList(
                new LambdaQueryWrapper<RetryTask>()
                        .in(RetryTask::getTaskStatus, RetryTaskStatusEnum.TERMINAL_STATUS_SET)
                        .eq(RetryTask::getNamespaceId, namespaceId)
                        .in(RetryTask::getId, ids));
        Assert.notEmpty(retryTasks, () -> new SnailJobServerException("Data does not exist"));
        Assert.isTrue(retryTasks.size() == ids.size(), () -> new SnailJobServerException("Data does not exist"));

        for (final RetryTask retryTask : retryTasks) {
            retryTaskLogMessageMapper.delete(
                    new LambdaQueryWrapper<RetryTaskLogMessage>()
                            .eq(RetryTaskLogMessage::getNamespaceId, namespaceId)
                            .eq(RetryTaskLogMessage::getGroupName, retryTask.getGroupName())
                            .eq(RetryTaskLogMessage::getRetryTaskId, retryTask.getId()));
        }
        return 1 == retryTaskMapper.deleteByIds(ids);
    }

    @Override
    public Boolean stopById(Long id) {

        RetryTask retryTask = retryTaskMapper.selectById(id);
        Assert.notNull(retryTask, () -> new SnailJobServerException("No executable tasks"));

        Retry retry = retryMapper.selectById(retryTask.getRetryId());
        Assert.notNull(retry, () -> new SnailJobServerException("Task does not exist"));

        TaskStopJobDTO taskStopJobDTO = RetryConverter.INSTANCE.toTaskStopJobDTO(retry);
        taskStopJobDTO.setOperationReason(RetryOperationReasonEnum.MANNER_STOP.getReason());
        taskStopJobDTO.setNeedUpdateTaskStatus(true);
        taskStopJobDTO.setMessage("User manually triggered stop");
        retryTaskStopHandler.stop(taskStopJobDTO);

        return true;
    }
}
