package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: opensnail
 * @date : 2022-03-11 19:44
 */
@AllArgsConstructor
@Getter
public enum HeadersEnum {

    HOST_ID("host-id"),
    HOST_IP("host-ip"),
    HOST_PORT("host-port"),
    GROUP_NAME("group-name"),
    CONTEXT_PATH("context-path"),
    REQUEST_ID("request-id"),
    VERSION("version"),
    HOST("Host"),
    NAMESPACE("namespace"),
    TOKEN("token"),
    LABEL("label"),
    ;

    private final String key;

}
