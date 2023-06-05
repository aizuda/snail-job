package com.aizuda.easy.retry.server.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.support.handler.CallbackRetryTaskHandler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTaskLog;
import com.aizuda.easy.retry.server.persistence.support.RetryTaskAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 重试完成执行器
 * 1、更新重试任务
 * 2、记录重试日志
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component("FinishActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class FinishActor extends AbstractActor  {

    public static final String BEAN_NAME = "FinishActor";

    @Autowired
    @Qualifier("retryTaskAccessProcessor")
    private RetryTaskAccess<RetryTask> retryTaskAccess;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private CallbackRetryTaskHandler callbackRetryTaskHandler;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTask.class, retryTask->{
            LogUtils.info(log, "FinishActor params:[{}]", retryTask);

            retryTask.setRetryStatus(RetryStatusEnum.FINISH.getStatus());

            try {
               transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                   @Override
                   protected void doInTransactionWithoutResult(TransactionStatus status) {
                       retryTaskAccess.updateRetryTask(retryTask);

                       if (TaskTypeEnum.RETRY.getType().equals(retryTask.getTaskType())) {
                           // 创建一个回调任务
                           callbackRetryTaskHandler.create(retryTask);
                       }
                   }
               });

            }catch (Exception e) {
                LogUtils.error(log, "更新重试任务失败", e);
            } finally {

                getContext().stop(getSelf());

                // 记录重试日志
                PageDTO<RetryTaskLog> retryTaskLogPageDTO = retryTaskLogMapper.selectPage(new PageDTO<>(1, 1),
                        new LambdaQueryWrapper<RetryTaskLog>()
                                .eq(RetryTaskLog::getIdempotentId, retryTask.getIdempotentId())
                                .orderByDesc(RetryTaskLog::getId));

                List<RetryTaskLog> records = retryTaskLogPageDTO.getRecords();
                if (!CollectionUtils.isEmpty(records)) {
                    RetryTaskLog retryTaskLog = records.get(0);
                    retryTaskLog.setRetryStatus(retryTask.getRetryStatus());
                    Assert.isTrue(1 ==  retryTaskLogMapper.updateById(retryTaskLog),
                        () -> new EasyRetryServerException("更新重试日志失败"));
                }
            }


        }).build();
    }

}
