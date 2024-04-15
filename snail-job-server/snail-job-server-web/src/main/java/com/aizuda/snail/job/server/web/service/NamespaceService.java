package com.aizuda.snail.job.server.web.service;

import com.aizuda.snail.job.server.web.model.base.PageResult;
import com.aizuda.snail.job.server.web.model.request.NamespaceQueryVO;
import com.aizuda.snail.job.server.web.model.request.NamespaceRequestVO;
import com.aizuda.snail.job.server.web.model.response.NamespaceResponseVO;

import java.util.List;

/**
 * @author: xiaowoniu
 * @date : 2023-11-21 15:14
 * @since : 2.5.0
 */
public interface NamespaceService {

    Boolean saveNamespace(NamespaceRequestVO namespaceRequestVO);

    Boolean updateNamespace(NamespaceRequestVO namespaceRequestVO);

    PageResult<List<NamespaceResponseVO>> getNamespacePage(NamespaceQueryVO queryVO);

    Boolean deleteNamespace(Long id);

    List<NamespaceResponseVO> getAllNamespace();

}
