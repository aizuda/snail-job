package com.aizuda.snailjob.client.job.core;

import com.aizuda.snailjob.client.model.ExecuteResult;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2024-06-26
 * @version : sj_1.1.0
 */
public interface MapHandler {

    ExecuteResult doMap(List<Object> taskList, String nextTaskName);
}
