package com.aizuda.snailjob.server.web.service;

import cn.hutool.core.lang.tree.Tree;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobTaskQueryVO;
import com.aizuda.snailjob.server.web.model.response.JobTaskResponseVO;

import java.util.List;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:54
 * @since ：2.4.0
 */
public interface JobTaskService {

    PageResult<List<JobTaskResponseVO>> getJobTaskPage(JobTaskQueryVO jobTaskQueryVO);

    List<JobTaskResponseVO> getTreeJobTask(JobTaskQueryVO jobTaskQueryVO);

}
