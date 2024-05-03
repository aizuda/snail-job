package com.aizuda.snailjob.common.core.alarm.attribute;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-05-04 16:13
 */
@Data
@Slf4j
public class EmailAttribute {

    private List<String> tos;

}
