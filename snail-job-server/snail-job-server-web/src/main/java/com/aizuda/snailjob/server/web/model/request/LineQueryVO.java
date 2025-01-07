package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.web.model.base.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LineQueryVO extends BaseQueryVO {
    /**
     * 组名称
     */
    private String groupName;

    /**
     * 时间间隔类型
     */
    private String type = "WEEK";
}
