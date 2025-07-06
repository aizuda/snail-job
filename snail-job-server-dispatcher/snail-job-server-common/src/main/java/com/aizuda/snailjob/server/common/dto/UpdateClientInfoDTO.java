package com.aizuda.snailjob.server.common.dto;

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
public class UpdateClientInfoDTO {

    private String groupName;
    private String namespaceId;
    private String hostId;
    private String hostIp;
    private String labels;

}
