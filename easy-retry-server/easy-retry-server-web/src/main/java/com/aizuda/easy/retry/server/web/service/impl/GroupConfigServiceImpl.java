package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.enums.IdGeneratorModeEnum;
import com.aizuda.easy.retry.server.common.enums.SystemModeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.retry.task.support.handler.ConfigVersionSyncHandler;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.request.UserSessionVO;
import com.aizuda.easy.retry.server.web.model.response.GroupConfigResponseVO;
import com.aizuda.easy.retry.server.web.service.GroupConfigService;
import com.aizuda.easy.retry.server.web.service.convert.GroupConfigConverter;
import com.aizuda.easy.retry.server.web.service.convert.GroupConfigResponseVOConverter;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.ConfigAccess;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.SequenceAllocMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
import com.aizuda.easy.retry.template.datasource.utils.DbUtils;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CRUD 组、场景、通知
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-22 14:54
 * @since 1.0.0
 */
@Service
public class GroupConfigServiceImpl implements GroupConfigService {

    @Autowired
    private ServerNodeMapper serverNodeMapper;
    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private SequenceAllocMapper sequenceAllocMapper;
    @Autowired
    @Lazy
    private ConfigVersionSyncHandler configVersionSyncHandler;
    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MybatisPlusProperties mybatisPlusProperties;
    @Autowired
    private RetryTaskMapper retryTaskMapper;

    @Override
    @Transactional
    public Boolean addGroup(UserSessionVO systemUser, GroupConfigRequestVO groupConfigRequestVO) {

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        Assert.isTrue(groupConfigAccess.count(new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getNamespaceId, systemUser.getNamespaceId())
                        .eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName())) == 0,
                () -> new EasyRetryServerException("GroupName已经存在 {}", groupConfigRequestVO.getGroupName()));

        // 保存组配置
        doSaveGroupConfig(systemUser, groupConfigRequestVO);

        // 保存生成唯一id配置
        doSaveSequenceAlloc(systemUser, groupConfigRequestVO);

        return Boolean.TRUE;
    }

    /**
     * 保存序号生成规则配置失败
     *
     * @param systemUser
     * @param groupConfigRequestVO 组、场景、通知配置类
     */
    private void doSaveSequenceAlloc(UserSessionVO systemUser, final GroupConfigRequestVO groupConfigRequestVO) {
        SequenceAlloc sequenceAlloc = new SequenceAlloc();
        sequenceAlloc.setGroupName(groupConfigRequestVO.getGroupName());
        sequenceAlloc.setNamespaceId(systemUser.getNamespaceId());
        sequenceAlloc.setStep(systemProperties.getStep());
        sequenceAlloc.setUpdateDt(LocalDateTime.now());
        Assert.isTrue(1 == sequenceAllocMapper.insert(sequenceAlloc),
                () -> new EasyRetryServerException("failed to save sequence generation rule configuration [{}].",
                        groupConfigRequestVO.getGroupName()));
    }

    @Override
    @Transactional
    public Boolean updateGroup(GroupConfigRequestVO groupConfigRequestVO) {

        String groupName = groupConfigRequestVO.getGroupName();
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        long count = groupConfigAccess.count(
                new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getNamespaceId, namespaceId)
                        .eq(GroupConfig::getGroupName, groupName));
        if (count <= 0) {
            return false;
        }

        GroupConfig groupConfig = GroupConfigConverter.INSTANCE.convert(groupConfigRequestVO);
        groupConfig.setDescription(Optional.ofNullable(groupConfigRequestVO.getDescription()).orElse(StrUtil.EMPTY));
        // 使用@TableField(value = "version", update= "%s+1") 进行更新version, 这里必须初始化一个值
        groupConfig.setVersion(1);
        Assert.isTrue(systemProperties.getTotalPartition() > groupConfigRequestVO.getGroupPartition(),
                () -> new EasyRetryServerException("分区超过最大分区. [{}]", systemProperties.getTotalPartition() - 1));
        Assert.isTrue(groupConfigRequestVO.getGroupPartition() >= 0,
                () -> new EasyRetryServerException("分区不能是负数."));

        // 校验retry_task_x和retry_dead_letter_x是否存在
        checkGroupPartition(groupConfig, namespaceId);

        Assert.isTrue(1 == groupConfigAccess.update(groupConfig,
                        new LambdaUpdateWrapper<GroupConfig>()
                                .eq(GroupConfig::getNamespaceId, namespaceId)
                                .eq(GroupConfig::getGroupName, groupName)),
                () -> new EasyRetryServerException("exception occurred while adding group. groupConfigVO[{}]",
                        groupConfigRequestVO));

        if (SystemModeEnum.isRetry(systemProperties.getMode())) {
            // 同步版本， 版本为0代表需要同步到客户端
            boolean add = configVersionSyncHandler.addSyncTask(groupName, namespaceId, 0);
            // 若添加失败则强制发起同步
            if (!add) {
                configVersionSyncHandler.syncVersion(groupName, namespaceId);
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean updateGroupStatus(String groupName, Integer status) {
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setGroupStatus(status);
        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        return groupConfigAccess.update(groupConfig,
                new LambdaUpdateWrapper<GroupConfig>()

                        .eq(GroupConfig::getGroupName, groupName)) == 1;
    }

    @Override
    public PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO) {

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();
        LambdaQueryWrapper<GroupConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupConfig::getNamespaceId, namespaceId);

        if (userSessionVO.isUser()) {
            queryWrapper.in(GroupConfig::getGroupName, userSessionVO.getGroupNames());
        }

        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            queryWrapper.like(GroupConfig::getGroupName, queryVO.getGroupName() + "%");
        }

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();

        queryWrapper.orderByDesc(GroupConfig::getId);
        PageDTO<GroupConfig> groupConfigPageDTO = groupConfigAccess.listPage(
                new PageDTO<>(queryVO.getPage(), queryVO.getSize()), queryWrapper);
        List<GroupConfig> records = groupConfigPageDTO.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PageResult<>(groupConfigPageDTO.getCurrent(), groupConfigPageDTO.getSize(),
                    groupConfigPageDTO.getTotal());
        }

        PageResult<List<GroupConfigResponseVO>> pageResult = new PageResult<>(groupConfigPageDTO.getCurrent(),
                groupConfigPageDTO.getSize(), groupConfigPageDTO.getTotal());

        List<GroupConfigResponseVO> responseVOList = GroupConfigResponseVOConverter.INSTANCE.toGroupConfigResponseVO(
                records);

        for (GroupConfigResponseVO groupConfigResponseVO : responseVOList) {
            Optional.ofNullable(IdGeneratorModeEnum.modeOf(groupConfigResponseVO.getIdGeneratorMode()))
                    .ifPresent(idGeneratorMode -> {
                        groupConfigResponseVO.setIdGeneratorModeName(idGeneratorMode.getDesc());
                    });
        }

        pageResult.setData(responseVOList);

        return pageResult;
    }

    private void doSaveGroupConfig(UserSessionVO systemUser, GroupConfigRequestVO groupConfigRequestVO) {
        GroupConfig groupConfig = GroupConfigConverter.INSTANCE.convert(groupConfigRequestVO);
        groupConfig.setCreateDt(LocalDateTime.now());
        groupConfig.setVersion(1);
        groupConfig.setNamespaceId(systemUser.getNamespaceId());
        groupConfig.setGroupName(groupConfigRequestVO.getGroupName());
        groupConfig.setDescription(Optional.ofNullable(groupConfigRequestVO.getDescription()).orElse(StrUtil.EMPTY));
        if (Objects.isNull(groupConfigRequestVO.getGroupPartition())) {
            groupConfig.setGroupPartition(
                    HashUtil.bkdrHash(groupConfigRequestVO.getGroupName()) % systemProperties.getTotalPartition());
        } else {
            Assert.isTrue(systemProperties.getTotalPartition() > groupConfigRequestVO.getGroupPartition(),
                    () -> new EasyRetryServerException("分区超过最大分区. [{}]", systemProperties.getTotalPartition() - 1));
            Assert.isTrue(groupConfigRequestVO.getGroupPartition() >= 0,
                    () -> new EasyRetryServerException("分区不能是负数."));
        }

        groupConfig.setBucketIndex(
                HashUtil.bkdrHash(groupConfigRequestVO.getGroupName()) % systemProperties.getBucketTotal());
        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        Assert.isTrue(1 == groupConfigAccess.insert(groupConfig),
                () -> new EasyRetryServerException("新增组异常异常 groupConfigVO[{}]", groupConfigRequestVO));

        // 校验retry_task_x和retry_dead_letter_x是否存在
        checkGroupPartition(groupConfig, systemUser.getNamespaceId());
    }

    /**
     * 校验retry_task_x和retry_dead_letter_x是否存在
     */
    private void checkGroupPartition(GroupConfig groupConfig, String namespaceId) {
        try {
            TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
            retryTaskAccess.count(groupConfig.getGroupName(), namespaceId,
                    new LambdaQueryWrapper<RetryTask>().eq(RetryTask::getId, 1));
        } catch (BadSqlGrammarException e) {
            Optional.ofNullable(e.getMessage()).ifPresent(s -> {
                if (s.contains("retry_task_" + groupConfig.getGroupPartition()) && s.contains("doesn't exist")) {
                    throw new EasyRetryServerException("分区:[{}] '未配置表retry_task_{}', 请联系管理员进行配置",
                            groupConfig.getGroupPartition(), groupConfig.getGroupPartition());
                }
            });
        }

        try {
            TaskAccess<RetryDeadLetter> retryTaskAccess = accessTemplate.getRetryDeadLetterAccess();
            retryTaskAccess.one(groupConfig.getGroupName(), namespaceId,
                    new LambdaQueryWrapper<RetryDeadLetter>().eq(RetryDeadLetter::getId, 1));
        } catch (BadSqlGrammarException e) {
            Optional.ofNullable(e.getMessage()).ifPresent(s -> {
                if (s.contains("retry_dead_letter_" + groupConfig.getGroupPartition()) && s.contains("doesn't exist")) {
                    throw new EasyRetryServerException("分区:[{}] '未配置表retry_dead_letter_{}', 请联系管理员进行配置",
                            groupConfig.getGroupPartition(), groupConfig.getGroupPartition());
                }
            });
        }
    }

    @Override
    public GroupConfigResponseVO getGroupConfigByGroupName(String groupName) {

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        GroupConfig groupConfig = groupConfigAccess.one(
                new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
                        .eq(GroupConfig::getGroupName, groupName));

        GroupConfigResponseVO groupConfigResponseVO = GroupConfigResponseVOConverter.INSTANCE.toGroupConfigResponseVO(
                groupConfig);

        Optional.ofNullable(IdGeneratorModeEnum.modeOf(groupConfig.getIdGeneratorMode())).ifPresent(idGeneratorMode -> {
            groupConfigResponseVO.setIdGeneratorModeName(idGeneratorMode.getDesc());
        });

        return groupConfigResponseVO;
    }

    @Override
    public List<GroupConfigResponseVO> getAllGroupConfigList(final List<String> namespaceIds) {
        if (CollectionUtils.isEmpty(namespaceIds)) {
            return new ArrayList<>();
        }

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();

        List<GroupConfig> groupConfigs = groupConfigAccess.list(new LambdaQueryWrapper<GroupConfig>()
                .select(GroupConfig::getGroupName, GroupConfig::getNamespaceId)
                .in(GroupConfig::getNamespaceId, namespaceIds));
        return GroupConfigResponseVOConverter.INSTANCE.toGroupConfigResponseVO(groupConfigs);
    }

    @Override
    public List<String> getAllGroupNameList() {

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        if (userSessionVO.isUser()) {
            return userSessionVO.getGroupNames();
        }

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        List<GroupConfig> groupConfigs = groupConfigAccess.list(new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getNamespaceId, userSessionVO.getNamespaceId())
                        .select(GroupConfig::getGroupName))
                .stream()
                .collect(Collectors.toList());

        return groupConfigs.stream().map(GroupConfig::getGroupName).collect(Collectors.toList());
    }

    @Override
    public List<String> getOnlinePods(String groupName) {
        List<ServerNode> serverNodes = serverNodeMapper.selectList(
                new LambdaQueryWrapper<ServerNode>()
                        .eq(ServerNode::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
                        .eq(ServerNode::getGroupName, groupName));
        return serverNodes.stream().map(serverNode -> serverNode.getHostIp() + ":" + serverNode.getHostPort())
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getTablePartitionList() {

        DataSource dataSource = jdbcTemplate.getDataSource();
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();

            DbTypeEnum dbType = DbUtils.getDbType();
            if (DbTypeEnum.ORACLE.getDb().equals(dbType.getDb())) {
                catalog = Optional.ofNullable(catalog).orElse(StrUtil.EMPTY).toUpperCase();
                schema = Optional.ofNullable(schema).orElse(StrUtil.EMPTY).toUpperCase();
            }

            DatabaseMetaData metaData = connection.getMetaData();
            String tablePrefix = Optional.ofNullable(mybatisPlusProperties.getGlobalConfig().getDbConfig().getTablePrefix()).orElse(StrUtil.EMPTY);
            ResultSet tables = metaData.getTables(catalog.toUpperCase(), schema.toUpperCase(), MessageFormat.format("{0}retry_task_%", tablePrefix), new String[]{"TABLE"});

            // 输出表名
            List<String> tableList = new ArrayList<>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableList.add(tableName);
            }

            return tableList.stream().map(ReUtil::getFirstNumber).filter(i ->
                            !Objects.isNull(i) && i <= systemProperties.getTotalPartition()).distinct()
                    .collect(Collectors.toList());
        } catch (SQLException ignored) {
        } finally {
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }

        // 兜底
        List<Integer> tableList = Lists.newArrayList();
        for (int i = 0; i < systemProperties.getTotalPartition(); i++) {
            tableList.add(i);
        }
        return tableList;
    }

}
