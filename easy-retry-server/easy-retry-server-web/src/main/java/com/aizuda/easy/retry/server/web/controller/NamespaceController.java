package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.web.annotation.LoginUser;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.NamespaceQueryVO;
import com.aizuda.easy.retry.server.web.model.request.NamespaceRequestVO;
import com.aizuda.easy.retry.server.web.model.response.NamespaceResponseVO;
import com.aizuda.easy.retry.server.web.service.NamespaceService;
import com.aizuda.easy.retry.template.datasource.persistence.po.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: xiaowoniu
 * @date : 2023-11-21 15:02
 * @since : 2.5.0
 */
@RestController
@RequestMapping("/namespace")
public class NamespaceController {

    @Autowired
    private NamespaceService namespaceService;

    @PostMapping
    public Boolean saveNamespace(@RequestBody @Validated NamespaceRequestVO namespaceRequestVO) {
        return namespaceService.saveNamespace(namespaceRequestVO);
    }

    @PutMapping
    public Boolean updateNamespace(@RequestBody @Validated NamespaceRequestVO namespaceRequestVO) {
        return namespaceService.updateNamespace(namespaceRequestVO);
    }

    @PutMapping
    public PageResult<List<NamespaceResponseVO>> getNamespacePage(NamespaceQueryVO queryVO) {
        return namespaceService.getNamespacePage(queryVO);
    }

    @PutMapping
    public List<NamespaceResponseVO> getNamespaceByUserId(@LoginUser SystemUser systemUser) {
        return namespaceService.getNamespaceByUserId(systemUser);
    }
}
