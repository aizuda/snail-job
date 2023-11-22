package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.NamespaceQueryVO;
import com.aizuda.easy.retry.server.web.model.request.NamespaceRequestVO;
import com.aizuda.easy.retry.server.web.model.response.NamespaceResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.SystemUser;

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
