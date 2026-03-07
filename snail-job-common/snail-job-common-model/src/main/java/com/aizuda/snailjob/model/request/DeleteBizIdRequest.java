package com.aizuda.snailjob.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 基于 bizId 的任务删除请求
 *
 * @author opensnail
 * @since 1.10.0
 */
@Data
public class DeleteBizIdRequest {

    @NotEmpty(message = "bizIds cannot be null")
    @Size(max = 100, message = "Maximum {max} deletions")
    private Set<String> bizIds;
}