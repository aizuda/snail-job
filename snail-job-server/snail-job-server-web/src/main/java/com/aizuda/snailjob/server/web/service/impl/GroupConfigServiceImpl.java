package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.enums.IdGeneratorModeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.ConfigVersionSyncHandler;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.ExportGroupVO;
import com.aizuda.snailjob.server.web.model.request.GroupConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.GroupConfigRequestVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.GroupConfigResponseVO;
import com.aizuda.snailjob.server.web.service.GroupConfigService;
import com.aizuda.snailjob.server.web.service.convert.GroupConfigConverter;
import com.aizuda.snailjob.server.web.service.convert.GroupConfigResponseVOConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.ConfigAccess;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.NamespaceMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.SequenceAllocMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import com.aizuda.snailjob.template.datasource.utils.DbUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CRUD 组、场景、通知
 *
 * @author: opensnail
 * @date : 2021-11-22 14:54
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Validated
public class GroupConfigServiceImpl implements GroupConfigService {

    private final ServerNodeMapper serverNodeMapper;
    private final AccessTemplate accessTemplate;
    private final SequenceAllocMapper sequenceAllocMapper;
    private final ConfigVersionSyncHandler configVersionSyncHandler;
    private final SystemProperties systemProperties;
    private final JdbcTemplate jdbcTemplate;
    private final NamespaceMapper namespaceMapper;

    @Override
    @Transactional
    public Boolean addGroup(GroupConfigRequestVO groupConfigRequestVO) {

        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        Assert.isTrue(groupConfigAccess.count(new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getNamespaceId, namespaceId)
                        .eq(GroupConfig::getGroupName, groupConfigRequestVO.getGroupName())) == 0,
                () -> new SnailJobServerException("GroupName已经存在 {}", groupConfigRequestVO.getGroupName()));

        // 保存组配置
        Boolean isSuccess = doSaveGroupConfig(namespaceId, groupConfigRequestVO);

        // 保存生成唯一id配置
        doSaveSequenceAlloc(namespaceId, groupConfigRequestVO);

        return isSuccess;
    }

    /**
     * 保存序号生成规则配置失败
     *
     * @param namespaceId          命名空间
     * @param groupConfigRequestVO 组、场景、通知配置类
     */
    private void doSaveSequenceAlloc(final String namespaceId, final GroupConfigRequestVO groupConfigRequestVO) {
        SequenceAlloc sequenceAlloc = new SequenceAlloc();
        sequenceAlloc.setGroupName(groupConfigRequestVO.getGroupName());
        sequenceAlloc.setNamespaceId(namespaceId);
        sequenceAlloc.setStep(systemProperties.getStep());
        sequenceAlloc.setUpdateDt(LocalDateTime.now());
        Assert.isTrue(1 == sequenceAllocMapper.insert(sequenceAlloc),
                () -> new SnailJobServerException("failed to save sequence generation rule configuration [{}].",
                        groupConfigRequestVO.getGroupName()));
    }

    @Override
    @Transactional
    public Boolean updateGroup(GroupConfigRequestVO groupConfigRequestVO) {

        List<Integer> tablePartitionList = getTablePartitionList();
        if (CollUtil.isEmpty(tablePartitionList)) {
            return Boolean.FALSE;
        }

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

        GroupConfig groupConfig = GroupConfigConverter.INSTANCE.toGroupConfig(groupConfigRequestVO);
        groupConfig.setDescription(Optional.ofNullable(groupConfigRequestVO.getDescription()).orElse(StrUtil.EMPTY));
        // 使用@TableField(value = "version", update= "%s+1") 进行更新version, 这里必须初始化一个值
        groupConfig.setVersion(1);
        // 不允许更新组
        groupConfig.setGroupName(null);
        // 不允许更新token
        groupConfig.setToken(null);
        Assert.isTrue(tablePartitionList.contains(groupConfigRequestVO.getGroupPartition()),
                () -> new SnailJobServerException("分区不存在. [{}]", tablePartitionList));
        Assert.isTrue(groupConfigRequestVO.getGroupPartition() >= 0,
                () -> new SnailJobServerException("分区不能是负数."));

        // 校验retry_task_x和retry_dead_letter_x是否存在
        checkGroupPartition(groupConfig, namespaceId);

        Assert.isTrue(1 == groupConfigAccess.update(groupConfig,
                        new LambdaUpdateWrapper<GroupConfig>()
                                .eq(GroupConfig::getNamespaceId, namespaceId)
                                .eq(GroupConfig::getGroupName, groupName)),
                () -> new SnailJobServerException("exception occurred while adding group. groupConfigVO[{}]",
                        groupConfigRequestVO));

        // 同步版本， 版本为0代表需要同步到客户端
        boolean add = configVersionSyncHandler.addSyncTask(groupName, namespaceId, 0);
        // 若添加失败则强制发起同步
        if (!add) {
            configVersionSyncHandler.syncVersion(groupName, namespaceId);
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
                        .eq(GroupConfig::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
                        .eq(GroupConfig::getGroupName, groupName)) == 1;
    }

    @Override
    public PageResult<List<GroupConfigResponseVO>> getGroupConfigForPage(GroupConfigQueryVO queryVO) {

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        String namespaceId = userSessionVO.getNamespaceId();

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        PageDTO<GroupConfig> groupConfigPageDTO = groupConfigAccess.listPage(
                new PageDTO<>(queryVO.getPage(), queryVO.getSize()),
                new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getNamespaceId, namespaceId)
                        .eq(Objects.nonNull(queryVO.getGroupStatus()), GroupConfig::getGroupStatus, queryVO.getGroupStatus())
                        .in(userSessionVO.isUser(), GroupConfig::getGroupName, userSessionVO.getGroupNames())
                        .likeRight(StrUtil.isNotBlank(queryVO.getGroupName()), GroupConfig::getGroupName,
                                StrUtil.trim(queryVO.getGroupName()))
                        .orderByDesc(GroupConfig::getId));
        List<GroupConfig> records = groupConfigPageDTO.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new PageResult<>(groupConfigPageDTO.getCurrent(), groupConfigPageDTO.getSize(),
                    groupConfigPageDTO.getTotal());
        }

        PageResult<List<GroupConfigResponseVO>> pageResult = new PageResult<>(groupConfigPageDTO.getCurrent(),
                groupConfigPageDTO.getSize(), groupConfigPageDTO.getTotal());

        List<GroupConfigResponseVO> responseVOList = GroupConfigResponseVOConverter.INSTANCE.convertList(
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

    private boolean doSaveGroupConfig(final String namespaceId, GroupConfigRequestVO groupConfigRequestVO) {
        List<Integer> tablePartitionList = getTablePartitionList();
        if (CollUtil.isEmpty(tablePartitionList)) {
            return Boolean.FALSE;
        }

        GroupConfig groupConfig = GroupConfigConverter.INSTANCE.toGroupConfig(groupConfigRequestVO);
        groupConfig.setCreateDt(LocalDateTime.now());
        groupConfig.setVersion(1);
        groupConfig.setNamespaceId(namespaceId);
        groupConfig.setGroupName(groupConfigRequestVO.getGroupName());
        groupConfig.setToken(groupConfigRequestVO.getToken());
        groupConfig.setDescription(Optional.ofNullable(groupConfigRequestVO.getDescription()).orElse(StrUtil.EMPTY));
        if (Objects.isNull(groupConfigRequestVO.getGroupPartition())) {
            groupConfig.setGroupPartition(
                    HashUtil.bkdrHash(groupConfigRequestVO.getGroupName()) % tablePartitionList.size());
        } else {
            Assert.isTrue(tablePartitionList.contains(groupConfigRequestVO.getGroupPartition()),
                    () -> new SnailJobServerException("分区不存在. [{}]", tablePartitionList));
            Assert.isTrue(groupConfigRequestVO.getGroupPartition() >= 0,
                    () -> new SnailJobServerException("分区不能是负数."));
        }

        groupConfig.setBucketIndex(
                HashUtil.bkdrHash(groupConfigRequestVO.getGroupName()) % systemProperties.getBucketTotal());
        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();
        Assert.isTrue(1 == groupConfigAccess.insert(groupConfig),
                () -> new SnailJobServerException("新增组异常异常 groupConfigVO[{}]", groupConfigRequestVO));

        // 校验retry_task_x和retry_dead_letter_x是否存在
        checkGroupPartition(groupConfig, namespaceId);

        return Boolean.TRUE;
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
                    throw new SnailJobServerException("分区:[{}] '未配置表retry_task_{}', 请联系管理员进行配置",
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
                    throw new SnailJobServerException("分区:[{}] '未配置表retry_dead_letter_{}', 请联系管理员进行配置",
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

        GroupConfigResponseVO groupConfigResponseVO = GroupConfigResponseVOConverter.INSTANCE.convert(
                groupConfig);

        Optional.ofNullable(IdGeneratorModeEnum.modeOf(groupConfig.getIdGeneratorMode())).ifPresent(idGeneratorMode -> {
            groupConfigResponseVO.setIdGeneratorModeName(idGeneratorMode.getDesc());
        });

        return groupConfigResponseVO;
    }

    @Override
    public List<GroupConfigResponseVO> getAllGroupConfigList(final List<String> namespaceIds) {

        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();

        List<GroupConfig> groupConfigs = groupConfigAccess.list(
                new LambdaQueryWrapper<GroupConfig>()
                        .select(GroupConfig::getGroupName, GroupConfig::getNamespaceId)
                        .in(CollUtil.isNotEmpty(namespaceIds), GroupConfig::getNamespaceId, namespaceIds));
        if (CollUtil.isEmpty(groupConfigs)) {
            return Collections.emptyList();
        }

        List<Namespace> namespaces = namespaceMapper.selectList(
                new LambdaQueryWrapper<Namespace>()
                        .in(Namespace::getUniqueId, StreamUtils.toSet(groupConfigs, GroupConfig::getNamespaceId)));

        Map<String, String> namespaceMap = StreamUtils.toMap(namespaces, Namespace::getUniqueId, Namespace::getName);

        List<GroupConfigResponseVO> groupConfigResponses = GroupConfigResponseVOConverter.INSTANCE.convertList(
                groupConfigs);
        for (final GroupConfigResponseVO groupConfigResponseVO : groupConfigResponses) {
            groupConfigResponseVO.setNamespaceName(namespaceMap.get(groupConfigResponseVO.getNamespaceId()));
        }

        return groupConfigResponses;
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
                .select(GroupConfig::getGroupName));

        return StreamUtils.toList(groupConfigs, GroupConfig::getGroupName);
    }

    @Override
    public List<String> getOnlinePods(String groupName) {
        List<ServerNode> serverNodes = serverNodeMapper.selectList(
                new LambdaQueryWrapper<ServerNode>()
                        .eq(ServerNode::getNamespaceId, UserSessionUtils.currentUserSession().getNamespaceId())
                        .eq(ServerNode::getGroupName, groupName));
        return StreamUtils.toList(serverNodes, serverNode -> serverNode.getHostIp() + ":" + serverNode.getHostPort());
    }

    @Override
    public List<Integer> getTablePartitionList() {
        DataSource dataSource = jdbcTemplate.getDataSource();
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();

            String tableNamePattern = "sj_retry_task_%";
            DbTypeEnum dbType = DbUtils.getDbType();
            if (DbTypeEnum.ORACLE.getDb().equals(dbType.getDb())) {
                tableNamePattern = tableNamePattern.toUpperCase();
            }

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(catalog, schema, tableNamePattern, new String[]{"TABLE"});

            // 输出表名
            List<String> tableList = new ArrayList<>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableList.add(tableName);
            }

            return tableList.stream().map(ReUtil::getFirstNumber).filter(i ->
                            !Objects.isNull(i)).distinct()
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

        return Lists.newArrayList();
    }

    @Override
    @Transactional
    public void importGroup(final List<GroupConfigRequestVO> requestList) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        Set<String> groupSet = StreamUtils.toSet(requestList, GroupConfigRequestVO::getGroupName);
        ConfigAccess<GroupConfig> groupConfigAccess = accessTemplate.getGroupConfigAccess();

        List<GroupConfig> configs = groupConfigAccess.list(new LambdaQueryWrapper<GroupConfig>()
                .select(GroupConfig::getGroupName)
                .eq(GroupConfig::getNamespaceId, namespaceId)
                .in(GroupConfig::getGroupName, groupSet));

        Assert.isTrue(CollUtil.isEmpty(configs),
                () -> new SnailJobServerException("导入失败. 原因: 组{}已存在",  StreamUtils.toSet(configs, GroupConfig::getGroupName)));

        for (final GroupConfigRequestVO groupConfigRequestVO : requestList) {

            // 保存组配置
            doSaveGroupConfig(namespaceId, groupConfigRequestVO);

            // 保存生成唯一id配置
            doSaveSequenceAlloc(namespaceId, groupConfigRequestVO);

        }

    }

    @Override
    public String exportGroup(ExportGroupVO exportGroupVO) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<GroupConfigRequestVO> allRequestList = Lists.newArrayList();
        PartitionTaskUtils.process((startId -> {
            List<GroupConfig> groupConfigs = accessTemplate.getGroupConfigAccess().listPage(new PageDTO<>(0, 100),
                new LambdaQueryWrapper<GroupConfig>()
                    .ge(GroupConfig::getId, startId)
                    .eq(GroupConfig::getNamespaceId, namespaceId)
                    .eq(Objects.nonNull(exportGroupVO.getGroupStatus()), GroupConfig::getGroupStatus, exportGroupVO.getGroupStatus())
                    .in(CollUtil.isNotEmpty(exportGroupVO.getGroupIds()), GroupConfig::getId, exportGroupVO.getGroupIds())
                    .likeRight(StrUtil.isNotBlank(exportGroupVO.getGroupName()), GroupConfig::getGroupName, StrUtil.trim(exportGroupVO.getGroupName()))
                    .orderByAsc(GroupConfig::getId)
            ).getRecords();
            return groupConfigs.stream().map(GroupConfigPartitionTask::new).toList();
        }), partitionTasks -> {
            List<GroupConfigPartitionTask> configPartitionTasks = (List<GroupConfigPartitionTask>) partitionTasks;
            List<GroupConfig> configs = StreamUtils.toList(configPartitionTasks, GroupConfigPartitionTask::getConfig);
            allRequestList.addAll(GroupConfigConverter.INSTANCE.toGroupConfigRequestVOs(configs));
        }, 0);

        return JsonUtil.toJsonString(allRequestList);
    }

    @EqualsAndHashCode(callSuper = true)
    @Getter
    private static class GroupConfigPartitionTask extends PartitionTask {
        // 这里就直接放GroupConfig为了后面若加字段不需要再这里在调整了
        private final GroupConfig config;
        public GroupConfigPartitionTask(@NotNull GroupConfig config) {
            this.config = config;
            setId(config.getId());
        }
    }
}
