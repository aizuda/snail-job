package com.aizuda.snailjob.common.core.enums;

/**
 * @author: opensnail
 * @date : 2024-08-23
 */
public enum RpcTypeEnum {

    GRPC,
    /**
     * @deprecated 1.3.0会兼容netty,预计会会在1.4.0仅支持GRPC
     */
    @Deprecated
    NETTY
}
