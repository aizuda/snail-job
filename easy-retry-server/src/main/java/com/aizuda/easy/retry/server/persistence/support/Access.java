package com.aizuda.easy.retry.server.persistence.support;

/**
 * @author www.byteblogs.com
 * @date 2023-07-20 22:47:57
 * @since 2.1.0
 */
public interface Access {

    boolean supports(String storageMedium);
}
