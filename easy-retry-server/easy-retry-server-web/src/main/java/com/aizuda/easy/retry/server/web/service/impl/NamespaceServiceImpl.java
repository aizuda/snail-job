package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.NamespaceQueryVO;
import com.aizuda.easy.retry.server.web.model.request.NamespaceRequestVO;
import com.aizuda.easy.retry.server.web.model.response.NamespaceResponseVO;
import com.aizuda.easy.retry.server.web.service.NamespaceService;
import com.aizuda.easy.retry.server.web.service.convert.NamespaceResponseVOConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.NamespaceMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.easy.retry.template.datasource.persistence.po.Namespace;
import com.aizuda.easy.retry.template.datasource.persistence.po.SystemUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: xiaowoniu
 * @date : 2023-11-21 15:42
 * @since : 2.5.0
 */
@Service
public class NamespaceServiceImpl implements NamespaceService {

    @Autowired
    private NamespaceMapper namespaceMapper;

    @Override
    public Boolean saveNamespace(final NamespaceRequestVO namespaceRequestVO) {
        Namespace namespace = new Namespace();
        namespace.setName(namespaceRequestVO.getName());
        if (StrUtil.isBlank(namespaceRequestVO.getUniqueId())) {
            namespace.setUniqueId(IdUtil.simpleUUID());
        }
        return 1 == namespaceMapper.insert(namespace);
    }

    @Override
    public Boolean updateNamespace(final NamespaceRequestVO namespaceRequestVO) {
        Long id = namespaceRequestVO.getId();
        Assert.notNull(id, () -> new EasyRetryServerException("参数错误"));

        Namespace namespace = new Namespace();
        namespace.setName(namespaceRequestVO.getName());
        namespace.setId(id);
        return 1 == namespaceMapper.updateById(namespace);
    }

    @Override
    public PageResult<List<NamespaceResponseVO>> getNamespacePage(final NamespaceQueryVO queryVO) {

        PageDTO<Namespace> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<Namespace> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryVO.getName())) {
            queryWrapper.like(Namespace::getName, queryVO.getName() + "%");
        }

        queryWrapper.eq(Namespace::getDeleted, StatusEnum.NO);
        queryWrapper.orderByDesc(Namespace::getId);
        PageDTO<Namespace> selectPage = namespaceMapper.selectPage(pageDTO, queryWrapper);
        return new PageResult<>(pageDTO,
            NamespaceResponseVOConverter.INSTANCE.toNamespaceResponseVOs(selectPage.getRecords()));
    }

    @Override
    public Boolean deleteNamespace(Long id) {
        return 1 == namespaceMapper.deleteById(id);
    }

    @Override
    public List<NamespaceResponseVO> getAllNamespace() {
        List<Namespace> namespaces = namespaceMapper.selectList(
            new LambdaQueryWrapper<Namespace>()
                .select(Namespace::getName, Namespace::getUniqueId)
        );
        return NamespaceResponseVOConverter.INSTANCE.toNamespaceResponseVOs(namespaces);
    }
}
