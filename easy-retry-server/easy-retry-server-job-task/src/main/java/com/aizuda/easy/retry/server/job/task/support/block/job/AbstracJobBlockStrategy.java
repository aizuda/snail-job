package com.aizuda.easy.retry.server.job.task.support.block.job;

import com.aizuda.easy.retry.server.job.task.support.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.enums.BlockStrategyEnum;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author: xiaowoniu
 * @date : 2024-01-18
 * @since : 2.6.0
 */
public abstract class AbstracJobBlockStrategy implements BlockStrategy , InitializingBean {

    @Override
    public void block(final BlockStrategyContext context) {
        doBlock(context);
    }

    protected abstract void doBlock(final BlockStrategyContext context);


    protected abstract BlockStrategyEnum blockStrategyEnum();

    @Override
    public void afterPropertiesSet() throws Exception {
        JobBlockStrategyFactory.registerBlockStrategy(blockStrategyEnum(), this);
    }
}
