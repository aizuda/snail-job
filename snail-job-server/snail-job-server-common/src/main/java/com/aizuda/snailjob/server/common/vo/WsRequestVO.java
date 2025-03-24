package com.aizuda.snailjob.server.common.vo;

import com.aizuda.snailjob.server.common.enums.WebSocketSceneEnum;
import lombok.Data;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.web.model.request
 * @Project：snail-job
 * @Date：2025/3/5 16:54
 * @Filename：WebSocketRequestVO
 * @since 1.5.0
 */
@Data
public class WsRequestVO {
    /**
     * wb类型
     */
    private String sid;

    /**
     * context
     */
    private String message;

    private WebSocketSceneEnum sceneEnum;
}
