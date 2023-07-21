package com.aizuda.easy.retry.server.support.handler;

import com.aizuda.easy.retry.server.persistence.support.LockAccess;
import com.aizuda.easy.retry.server.support.cache.CacheLockRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author www.byteblogs.com
 * @date 2023-07-20 22:28:35
 * @since 2.1.0
 */
@Component
public class DistributedLockHandler {
    @Autowired
    private LockAccess lockAccess;


}
