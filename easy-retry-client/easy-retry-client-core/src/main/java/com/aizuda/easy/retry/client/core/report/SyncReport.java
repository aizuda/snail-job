package com.aizuda.easy.retry.client.core.report;

import com.aizuda.easy.retry.client.common.cache.GroupVersionCache;
import com.aizuda.easy.retry.client.common.config.EasyRetryProperties;
import com.aizuda.easy.retry.client.common.proxy.RequestBuilder;
import com.aizuda.easy.retry.client.common.client.NettyClient;
import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;
import com.aizuda.easy.retry.common.core.alarm.Alarm;
import com.aizuda.easy.retry.common.core.alarm.AlarmContext;
import com.aizuda.easy.retry.common.core.alarm.EasyRetryAlarmFactory;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.NotifySceneEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.EnvironmentUtils;
import com.aizuda.easy.retry.common.core.util.NetUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.model.dto.ConfigDTO;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 同步上报数据
 *
 * @author www.byteblogs.com
 * @date 2023-05-15
 * @since 1.3.0
 */
@Component
@Slf4j
public class SyncReport extends AbstractReport {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String reportErrorTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 同步上报异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n"
            ;

    @Autowired
    private EasyRetryProperties easyRetryProperties;

    @Override
    public boolean supports(boolean async) {
        return !async;
    }

    @Override
    public boolean doReport(RetryerInfo retryerInfo, Object[] params) {

        return syncReport(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params, retryerInfo.getTimeout(), retryerInfo.getUnit());
    }

    /**
     * 异步上报到服务端, 若当前处于远程重试阶段不会进行执行上报
     */
    public Boolean syncReport(String scene, String targetClassName, Object[] args, long timeout, TimeUnit unit) {

        RetryTaskDTO retryTaskDTO = buildRetryTaskDTO(scene, targetClassName, args);

        NettyClient client = RequestBuilder.<NettyClient, NettyResult>newBuilder()
                .client(NettyClient.class)
                .async(Boolean.FALSE)
                .timeout(timeout)
                .unit(unit)
                .build();

        try {
            NettyResult result = client.reportRetryInfo(Collections.singletonList(retryTaskDTO));
            EasyRetryLog.LOCAL.debug("Data report result result:[{}]", JsonUtil.toJsonString(result));
            return (Boolean) result.getData();
        } catch (Exception e) {
            sendMessage(e);
            throw e;
        }

    }

    private void sendMessage(Throwable e) {

        try {
            ConfigDTO.Notify notifyAttribute = GroupVersionCache.getNotifyAttribute(NotifySceneEnum.CLIENT_REPORT_ERROR.getNotifyScene());
            if (Objects.isNull(notifyAttribute)) {
                return;
            }

            AlarmContext context = AlarmContext.build()
                    .text(reportErrorTextMessageFormatter,
                            EnvironmentUtils.getActiveProfile(),
                            NetUtil.getLocalIpStr(),
                            easyRetryProperties.getNamespace(),
                            EasyRetryProperties.getGroup(),
                            LocalDateTime.now().format(formatter),
                            e.getMessage())
                    .title("同步上报异常:[{}]", EasyRetryProperties.getGroup())
                    .notifyAttribute(notifyAttribute.getNotifyAttribute());

            EasyRetryAlarmFactory easyRetryAlarmFactory = SpringContext.getBeanByType(EasyRetryAlarmFactory.class);
            Alarm<AlarmContext> alarmType = easyRetryAlarmFactory.getAlarmType(notifyAttribute.getNotifyType());
            alarmType.asyncSendMessage(context);
        } catch (Exception e1) {
            EasyRetryLog.LOCAL.error("客户端发送组件异常告警失败", e1);
        }

    }

}
