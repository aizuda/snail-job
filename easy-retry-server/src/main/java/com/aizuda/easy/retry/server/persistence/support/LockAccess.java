package com.aizuda.easy.retry.server.persistence.support;

import net.javacrumbs.shedlock.core.LockConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @author www.byteblogs.com
 * @date 2023-07-20 22:45:41
 * @since 2.1.0
 */
public interface LockAccess extends Access {

    boolean insertRecord();

    boolean updateRecord();

    void unlock();

}
