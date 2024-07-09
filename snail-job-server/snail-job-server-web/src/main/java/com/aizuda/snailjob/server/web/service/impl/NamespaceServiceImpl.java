package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NamespaceQueryVO;
import com.aizuda.snailjob.server.web.model.request.NamespaceRequestVO;
import com.aizuda.snailjob.server.web.model.response.NamespaceResponseVO;
import com.aizuda.snailjob.server.web.service.NamespaceService;
import com.aizuda.snailjob.server.web.service.convert.NamespaceResponseVOConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NamespaceMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Namespace;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.DEFAULT_NAMESPACE;

/**
 * @author: xiaowoniu
 * @date : 2023-11-21 15:42
 * @since : 2.5.0
 */
@Service
@RequiredArgsConstructor
public class NamespaceServiceImpl implements NamespaceService {
    private final NamespaceMapper namespaceMapper;
    private final GroupConfigMapper groupConfigMapper;

    @Override
    public Boolean saveNamespace(final NamespaceRequestVO namespaceRequestVO) {
        String uniqueId = namespaceRequestVO.getUniqueId();

        if (StrUtil.isNotBlank(namespaceRequestVO.getUniqueId())) {
            Pattern pattern = Pattern.compile(SystemConstants.REGEXP);
            Matcher matcher = pattern.matcher(uniqueId);
            Assert.isTrue(matcher.matches(), () -> new SnailJobServerException("仅支持长度为1~64字符且类型为数字、字母、下划线和短横线"));

            Assert.isTrue(namespaceMapper.selectCount(
                            new LambdaQueryWrapper<Namespace>()
                                    .eq(Namespace::getUniqueId, namespaceRequestVO.getUniqueId())) == 0,
                    () -> new SnailJobServerException("空间唯一标记已经存在 {}", namespaceRequestVO.getUniqueId()));
        }

        Namespace namespace = new Namespace();
        namespace.setName(namespaceRequestVO.getName());
        if (StrUtil.isBlank(uniqueId)) {
            namespace.setUniqueId(IdUtil.simpleUUID());
        } else {
            namespace.setUniqueId(uniqueId);
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
    public Boolean deleteByUniqueId(String uniqueId) {

        Assert.isFalse(DEFAULT_NAMESPACE.equals(uniqueId), ()-> new SnailJobServerException("默认空间禁止删除"));

        Assert.isTrue(CollUtil.isEmpty(groupConfigMapper.selectList(new PageDTO<>(1, 1), new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getNamespaceId, uniqueId).orderByAsc(GroupConfig::getId))),
                () -> new SnailJobServerException("存在未删除的组配置任务. 请先删除当前关联的组配置再重试删除"));

        Assert.isTrue(1 == namespaceMapper.delete(new LambdaQueryWrapper<Namespace>().eq(Namespace::getUniqueId, uniqueId)),
                () -> new SnailJobServerException("删除命名空间失败"));

        return Boolean.TRUE;
    }

    @Override
    public List<NamespaceResponseVO> getAllNamespace() {
        List<Namespace> namespaces = namespaceMapper.selectList(
                new LambdaQueryWrapper<Namespace>()
                        .select(Namespace::getName, Namespace::getUniqueId)
                        .eq(Namespace::getDeleted, StatusEnum.NO.getStatus())
                        .orderByDesc(Namespace::getId)
        );
        return NamespaceResponseVOConverter.INSTANCE.convertList(namespaces);
    }
}
