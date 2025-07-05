package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-15 16:06:20
 * @since 2.4.0
 */
@Data
public class ServerNodeLabelsUpdateRequestVO {

    @NotNull(message = "id cannot be null")
    private Long id;

    @NotNull(message = "labels cannot be null")
    @Size(max = 512, message = "labels length must be less than or equal to 512")
    private String labels;

}
