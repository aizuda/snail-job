package com.aizuda.easy.retry.server.job.task.handler.callback;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author www.byteblogs.com
 * @date 2023-10-03 23:12:33
 * @since 2.4.0
 */
public abstract class AbstractClientCallbackHandler implements ClientCallbackHandler, InitializingBean {

    @Override
    @Transactional
    public void callback(ClientCallbackContext context) {
        doCallback(context);
    }

    protected abstract void doCallback(ClientCallbackContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        ClientCallbackFactory.registerJobExecutor(getTaskInstanceType(), this);
    }
}
