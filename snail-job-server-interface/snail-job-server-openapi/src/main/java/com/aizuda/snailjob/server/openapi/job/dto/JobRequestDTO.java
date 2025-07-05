package com.aizuda.snailjob.server.openapi.job.dto;

import com.aizuda.snailjob.server.service.dto.JobRequestBaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobRequestDTO extends JobRequestBaseDTO {
    /**
     * 命名空间
     */
    @NotBlank(message = "namespaceId cannot be null")
    private String namespaceId;

}
