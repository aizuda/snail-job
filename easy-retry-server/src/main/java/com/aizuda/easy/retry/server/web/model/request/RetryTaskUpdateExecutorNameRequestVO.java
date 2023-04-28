package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

/**
 * 更新执行器名称
 *
 * @author www.byteblogs.com
 * @date 2022-09-29
 */
@Data
public class RetryTaskUpdateExecutorNameRequestVO {

    /**
     * 组名称
     */
    @NotBlank(message = "groupName 不能为空")
    private String groupName;

    @NotBlank(message = "scene 不能为空")
    private String sceneName;

    @NotBlank(message = "executorName 不能为空")
    private String executorName;

    /**
     * 重试表id
     */
    private List<Long> ids;

}
