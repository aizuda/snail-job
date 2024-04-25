/*
 * Copyright (c) 2024 .
 *
 * SnailJob - 灵活，可靠和快速的分布式任务重试和分布式任务调度平台
 * > ✅️ 可重放，可管控、为提高分布式业务系统一致性的分布式任务重试平台
 * > ✅️ 支持秒级、可中断、可编排的高性能分布式任务调度平台
 *
 * Aizuda/SnailJob 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点:
 *
 * 1. Aizuda/SnailJob已申请知识产权保护,二次开发如用于开源竞品请先联系群主沟通，禁止任何变相的二开行为，未经审核视为侵权;
 * 2. 不得修改产品相关代码的源码头注释和出处;
 * 3. 不得进行简单修改包装声称是自己的产品;
 * 4. 不得应用于危害国家安全、荣誉和利益的行为，不能以任何形式用于非法目的;
 *
 */
package com.aizuda.snailjob.client.common.appender;

import com.aizuda.snailjob.client.common.log.report.LogReportFactory;
import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.client.common.rpc.client.NettyChannel;
import com.aizuda.snailjob.common.log.dto.LogContentDTO;
import com.aizuda.snailjob.common.log.constant.LogFieldConstants;
import com.aizuda.snailjob.client.common.log.report.LogReportFactory;
import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.client.common.rpc.client.NettyChannel;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.Objects;
import java.util.Optional;

/**
 * @author wodeyangzipingpingwuqi
 * @date 2023-12-27
 * @since 2.6.0
 */
public class SnailLog4jAppender extends AppenderSkeleton {

    @Override
    public void activateOptions() {
        super.activateOptions();
    }

    @Override
    protected void append(LoggingEvent event) {

        // Not job context
        if (Objects.isNull(SnailJobLogManager.getLogMeta()) || Objects.isNull(MDC.get(LogFieldConstants.MDC_REMOTE))) {
            return;
        }

        MDC.remove(LogFieldConstants.MDC_REMOTE);
        LogContentDTO logContentDTO = new LogContentDTO();
        logContentDTO.addTimeStamp(event.getTimeStamp());
        logContentDTO.addLevelField(event.getLevel().toString());
        logContentDTO.addThreadField(event.getThreadName());
        logContentDTO.addMessageField(event.getMessage().toString());
        logContentDTO.addLocationField(event.getLocationInformation().fullInfo);
        logContentDTO.addThrowableField(getThrowableField(event));
        logContentDTO.addHostField(NettyChannel.getClientHost());
        logContentDTO.addPortField(NettyChannel.getClientPort());

        // slidingWindow syncReportLog
        Optional.ofNullable(LogReportFactory.get()).ifPresent(logReport -> logReport.report(logContentDTO));
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    private String getThrowableField(LoggingEvent event) {
        String throwable = getThrowableStr(event);
        if (throwable != null) {
            return throwable;
        }
        return null;
    }

    private String getThrowableStr(LoggingEvent event) {
        ThrowableInformation throwable = event.getThrowableInformation();
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        int stackDeep = 0;
        for (String s : throwable.getThrowableStrRep()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(System.getProperty("line.separator"));
            }
            sb.append(s);
            // 最多显示30行
            if (++stackDeep >= 30) {
                break;
            }
        }
        return sb.toString();
    }
}
