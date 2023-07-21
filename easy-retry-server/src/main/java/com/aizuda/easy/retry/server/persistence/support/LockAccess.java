package com.aizuda.easy.retry.server.persistence.support;

import com.aizuda.easy.retry.server.dto.LockConfig;

/**
 * @author www.byteblogs.com
 * @date 2023-07-20 22:45:41
 * @since 2.1.0
 */
public interface LockAccess extends Access {

    boolean lock(LockConfig lockConfig);

    boolean unlock(LockConfig lockConfig);

}
