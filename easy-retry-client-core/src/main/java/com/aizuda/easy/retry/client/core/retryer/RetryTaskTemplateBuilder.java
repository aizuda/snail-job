package com.aizuda.easy.retry.client.core.retryer;

import com.aizuda.easy.retry.client.core.report.ReportHandler;
import com.aizuda.easy.retry.client.core.strategy.ExecutorMethod;
import com.aizuda.easy.retry.common.core.context.SpringContext;

import java.util.concurrent.TimeUnit;

/**
 * 构建重试模板对象
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-10 10:17
 */
public class RetryTaskTemplateBuilder {

    private Class<? extends ExecutorMethod> executorMethodClass;
    private String scene;
    private Object[] params;
    private long timeout = 60*1000;
    private TimeUnit unit = TimeUnit.MILLISECONDS;

    public static RetryTaskTemplateBuilder newBuilder() {
        return new RetryTaskTemplateBuilder();
    }

    public RetryTaskTemplateBuilder withScene(String scene) {
        this.scene = scene;
        return this;
    }

    public RetryTaskTemplateBuilder withExecutorMethod(Class<? extends ExecutorMethod> executorMethod) {
        this.executorMethodClass = executorMethod;
        return this;
    }

    public RetryTaskTemplateBuilder withParam(Object params) {
        this.params = new Object[]{params};
        return this;
    }

    public RetryTaskTemplateBuilder timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public RetryTaskTemplateBuilder unit(TimeUnit unit) {
        this.unit = unit;
        return this;
    }

    public EasyRetryTemplate build() {
        EasyRetryTemplate easyRetryTemplate = new EasyRetryTemplate();
        easyRetryTemplate.setParams(params);
        easyRetryTemplate.setExecutorMethodClass(executorMethodClass);
        easyRetryTemplate.setScene(scene);
        easyRetryTemplate.setTimeout(timeout);
        easyRetryTemplate.setUnit(unit);
        ReportHandler reportHandler = SpringContext.CONTEXT.getBean(ReportHandler.class);
        easyRetryTemplate.setReportHandler(reportHandler);
        return easyRetryTemplate;
    }
}
