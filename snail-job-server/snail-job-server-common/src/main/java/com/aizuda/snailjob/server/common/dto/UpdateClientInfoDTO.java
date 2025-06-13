package com.aizuda.snailjob.server.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-06-13
 */
@Data
@Builder
public class UpdateClientInfoDTO {

    private String groupName;
    private String namespaceId;
    private String hostId;
    private String hostIp;
    private String labels;

}
