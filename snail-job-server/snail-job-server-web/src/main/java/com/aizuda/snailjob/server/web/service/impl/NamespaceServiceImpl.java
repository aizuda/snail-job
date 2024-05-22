package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NamespaceQueryVO;
import com.aizuda.snailjob.server.web.model.request.NamespaceRequestVO;
import com.aizuda.snailjob.server.web.model.response.NamespaceResponseVO;
import com.aizuda.snailjob.server.web.service.NamespaceService;
import com.aizuda.snailjob.server.web.service.convert.NamespaceResponseVOConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NamespaceMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Namespace;
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

        if (StrUtil.isNotBlank(namespaceRequestVO.getUniqueId())) {
            Assert.isTrue(namespaceMapper.selectCount(
                            new LambdaQueryWrapper<Namespace>()
                                    .eq(Namespace::getUniqueId, namespaceRequestVO.getUniqueId())) == 0,
                    () -> new SnailJobServerException("空间唯一标记已经存在 {}", namespaceRequestVO.getUniqueId()));
        }

        Namespace namespace = new Namespace();
        namespace.setName(namespaceRequestVO.getName());
        if (StrUtil.isBlank(namespaceRequestVO.getUniqueId())) {
            namespace.setUniqueId(IdUtil.simpleUUID());
        } else {
            namespace.setUniqueId(namespaceRequestVO.getUniqueId());
        }
        return 1 == namespaceMapper.insert(namespace);
    }

    @Override
    public Boolean updateNamespace(final NamespaceRequestVO namespaceRequestVO) {
        Long id = namespaceRequestVO.getId();
        Assert.notNull(id, () -> new SnailJobServerException("参数错误"));

        Namespace namespace = new Namespace();
        namespace.setName(namespaceRequestVO.getName());
        namespace.setId(id);
        return 1 == namespaceMapper.updateById(namespace);
    }

    @Override
    public PageResult<List<NamespaceResponseVO>> getNamespacePage(final NamespaceQueryVO queryVO) {

        PageDTO<Namespace> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        String keywords = StrUtil.trim(queryVO.getKeyword());

        PageDTO<Namespace> selectPage = namespaceMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<Namespace>()
                        .eq(Namespace::getDeleted, StatusEnum.NO.getStatus())
                        .and(StrUtil.isNotBlank(keywords), w ->
                                w.likeRight(Namespace::getName, keywords)
                                        .or().likeRight(Namespace::getUniqueId, keywords))
                        .orderByDesc(Namespace::getId));
        return new PageResult<>(pageDTO,
                NamespaceResponseVOConverter.INSTANCE.convertList(selectPage.getRecords()));
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
        return NamespaceResponseVOConverter.INSTANCE.convertList(namespaces);
    }
}
