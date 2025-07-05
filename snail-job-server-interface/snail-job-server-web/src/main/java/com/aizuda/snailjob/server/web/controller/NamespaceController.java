package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NamespaceQueryVO;
import com.aizuda.snailjob.server.web.model.request.NamespaceRequestVO;
import com.aizuda.snailjob.server.web.model.response.NamespaceResponseVO;
import com.aizuda.snailjob.server.web.service.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @LoginRequired(role = RoleEnum.ADMIN)
    @PostMapping
    public Boolean saveNamespace(@RequestBody @Validated NamespaceRequestVO namespaceRequestVO) {
        return namespaceService.saveNamespace(namespaceRequestVO);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @PutMapping
    public Boolean updateNamespace(@RequestBody @Validated NamespaceRequestVO namespaceRequestVO) {
        return namespaceService.updateNamespace(namespaceRequestVO);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @GetMapping("list")
    public PageResult<List<NamespaceResponseVO>> getNamespacePage(NamespaceQueryVO queryVO) {
        return namespaceService.getNamespacePage(queryVO);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @DeleteMapping("{uniqueId}")
    public Boolean deleteByUniqueId(@PathVariable("uniqueId") String uniqueId) {
        return namespaceService.deleteByUniqueId(uniqueId);
    }

    @LoginRequired(role = RoleEnum.ADMIN)
    @GetMapping("/all")
    public List<NamespaceResponseVO> getAllNamespace() {
        return namespaceService.getAllNamespace();
    }
}
