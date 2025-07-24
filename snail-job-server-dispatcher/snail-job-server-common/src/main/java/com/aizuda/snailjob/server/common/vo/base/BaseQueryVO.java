package com.aizuda.snailjob.server.common.vo.base;

import cn.hutool.core.util.ObjUtil;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author opensnail
 * @date 2022-02-27
 * @since 2.0
 */
@Data
@Deprecated
public class BaseQueryVO {

    /**
     * 当前页码
     */
    private int page = 1;

    /**
     * 每页条数
     */
    private int size = 10;

    /**
     * 时间区间, 用于筛选 createDt 字段,
     * 前端传入使用 SpringBoot 默认 ISO 8601 格式: yyyy-MM-dd'T'HH:mm:ss
     */
    private LocalDateTime[] datetimeRange;

    /**
     * 起始时间
     *
     * @return 起始时间
     */
    public LocalDateTime getStartDt() {
        return ObjUtil.isEmpty(datetimeRange) ? null : datetimeRange[0];
    }

    /**
     * 结束时间
     *
     * @return 结束时间
     */
    public LocalDateTime getEndDt() {
        return ObjUtil.isEmpty(datetimeRange) ? null : datetimeRange[1];
    }

}
