package com.aizuda.easy.retry.client.core.retryer;

import com.aizuda.easy.retry.client.core.RetryOperations;
import com.aizuda.easy.retry.client.core.report.ReportHandler;
import com.aizuda.easy.retry.client.core.strategy.ExecutorMethod;

import java.util.concurrent.TimeUnit;

/**
 * 手动生成重试任务模板类
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-09 16:30
 * @since 1.3.0
 */
public class EasyRetryTemplate implements RetryOperations {

    private Class<? extends ExecutorMethod> executorMethodClass;
    private String scene;
    private Object[] params;
    private ReportHandler reportHandler;
    private long timeout = 60 * 1000;
    private TimeUnit unit = TimeUnit.MILLISECONDS;

    @Override
    public void generateAsyncTask() {
        generateAsyncTask(Boolean.FALSE);
    }

    @Override
    public void generateAsyncTask(boolean forceReport) {
        if (forceReport) {
            reportHandler.asyncReportWithForce(scene, executorMethodClass.getName(), params);
        } else {
            reportHandler.asyncReport(scene, executorMethodClass.getName(), params);
        }
    }

    @Override
    public Boolean generateSyncTask() {
       return generateSyncTask(Boolean.FALSE);
    }

    @Override
    public Boolean generateSyncTask(final boolean forceReport) {
        if (forceReport) {
           return reportHandler.syncReportWithForce(scene, executorMethodClass.getName(), params, timeout, unit);
        } else {
            return reportHandler.syncReport(scene, executorMethodClass.getName(), params,  timeout, unit);
        }
    }

    protected void setExecutorMethodClass(
        final Class<? extends ExecutorMethod> executorMethodClass) {
        this.executorMethodClass = executorMethodClass;
    }

    protected void setScene(final String scene) {
        this.scene = scene;
    }

    protected void setParams(final Object[] params) {
        this.params = params;
    }

    protected void setReportHandler(final ReportHandler reportHandler) {
        this.reportHandler = reportHandler;
    }

    protected void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    protected void setUnit(final TimeUnit unit) {
        this.unit = unit;
    }
}
