package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author opensnail
 * @date 2022-03-05
 * @since 2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SceneConfigQueryVO extends BaseQueryVO {
    private String groupName;
    private String sceneName;
}
