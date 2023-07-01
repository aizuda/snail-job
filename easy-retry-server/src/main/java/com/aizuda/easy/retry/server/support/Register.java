package com.aizuda.easy.retry.server.support;

import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.server.support.register.RegisterContext;

/**
 * @author www.byteblogs.com
 * @date 2023-06-07
 * @since 2.0
 */
public interface Register {

    /**
     * 节点类型  see: {@link NodeTypeEnum}
     *
     * @param type 1. 客户端 2.服务端
     * @return
     */
    boolean supports(int type);

    /**
     * 执行注册
     *
     * @return
     */
    boolean register(RegisterContext context);
}
