package com.aizuda.snail.job.server.common.lock.persistence;

import com.aizuda.snail.job.server.common.dto.LockConfig;

import java.time.LocalDateTime;

/**
 * @author xiaowoniu
 * @date 2024-01-11 21:38:52
 * @since 2.6.0
 */
public interface LockStorage {

    boolean supports(String storageMedium);

    /**
     * 创建锁记录
     *
     * @param lockConfig 锁配置
     * @return
     */
    boolean createLock(LockConfig lockConfig);

    /**
     * 更新锁记录
     *
     * @param lockConfig 锁配置
     * @return
     */
    boolean renewal(LockConfig lockConfig);

    /**
     * 删除锁记录释放锁
     *
     * @param lockName 锁名称
     * @return
     */
    boolean releaseLockWithDelete(String lockName);

    /**
     * 更新锁定时长释放锁
     *
     * @param lockName    锁名称
     * @param lockAtLeast 最少锁定时长
     * @return
     */
    boolean releaseLockWithUpdate(String lockName, LocalDateTime lockAtLeast);

}
