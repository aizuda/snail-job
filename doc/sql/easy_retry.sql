CREATE TABLE `group_config`
(
    `id`                bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name`        varchar(64)  NOT NULL DEFAULT '' COMMENT '组名称',
    `description`       varchar(256) NOT NULL COMMENT '组描述',
    `group_status`      tinyint(4) NOT NULL DEFAULT '0' COMMENT '组状态 0、未启用 1、启用',
    `version`           int(11) NOT NULL COMMENT '版本号',
    `group_partition`   int(11) NOT NULL COMMENT '分区',
    `route_key`         tinyint(4) NOT NULL COMMENT '路由策略',
    `id_generator_mode` tinyint(4) NOT NULL DEFAULT '1' COMMENT '唯一id生成模式 默认号段模式',
    `create_dt`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`group_name`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='组配置'
;

CREATE TABLE `notify_config`
(
    `id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name`       varchar(64)  NOT NULL COMMENT '组名称',
    `notify_type`      tinyint(4) NOT NULL DEFAULT '0' COMMENT '通知类型 1、钉钉 2、邮件 3、企业微信',
    `notify_attribute` varchar(512) NOT NULL COMMENT '配置属性',
    `notify_threshold` int(11) NOT NULL DEFAULT '0' COMMENT '通知阈值',
    `notify_scene`     tinyint(4) NOT NULL DEFAULT '0' COMMENT '通知场景',
    `description`      varchar(256) NOT NULL DEFAULT '' COMMENT '描述',
    `create_dt`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY                `idx_group_name` (`group_name`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='通知配置'
;

CREATE TABLE `retry_dead_letter_0`
(
    `id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `unique_id`     varchar(64)  NOT NULL COMMENT '同组下id唯一',
    `group_name`    varchar(64)  NOT NULL COMMENT '组名称',
    `scene_name`    varchar(64)  NOT NULL COMMENT '场景id',
    `idempotent_id` varchar(64)  NOT NULL COMMENT '幂等id',
    `biz_no`        varchar(64)  NOT NULL DEFAULT '' COMMENT '业务编号',
    `executor_name` varchar(512) NOT NULL DEFAULT '' COMMENT '执行器名称',
    `args_str`      text         NOT NULL COMMENT '执行方法参数',
    `ext_attrs`     text         NOT NULL COMMENT '扩展字段',
    `create_dt`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY             `idx_group_name_scene_name` (`group_name`, `scene_name`),
    KEY             `idx_idempotent_id` (`idempotent_id`),
    KEY             `idx_biz_no` (`biz_no`),
    UNIQUE KEY `uk_name_unique_id` (`group_name`, `unique_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='重试死信队列'
;

CREATE TABLE `retry_task_0`
(
    `id`              bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `unique_id`       varchar(64)  NOT NULL COMMENT '同组下id唯一',
    `group_name`      varchar(64)  NOT NULL COMMENT '组名称',
    `scene_name`      varchar(64)  NOT NULL COMMENT '场景名称',
    `idempotent_id`   varchar(64)  NOT NULL COMMENT '幂等id',
    `biz_no`          varchar(64)  NOT NULL DEFAULT '' COMMENT '业务编号',
    `executor_name`   varchar(512) NOT NULL DEFAULT '' COMMENT '执行器名称',
    `args_str`        text         NOT NULL COMMENT '执行方法参数',
    `ext_attrs`       text         NOT NULL COMMENT '扩展字段',
    `next_trigger_at` datetime     NOT NULL COMMENT '下次触发时间',
    `retry_count`     int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
    `retry_status`    tinyint(4) NOT NULL DEFAULT '0' COMMENT '重试状态 0、重试中 1、成功 2、最大重试次数',
    `create_dt`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY               `idx_group_name_scene_name` (`group_name`, `scene_name`),
    KEY               `idx_retry_status` (`retry_status`),
    KEY               `idx_idempotent_id` (`idempotent_id`),
    UNIQUE KEY `uk_name_unique_id` (`group_name`, `unique_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='重试表'
;

CREATE TABLE `retry_task_log`
(
    `id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `unique_id`     varchar(64)  NOT NULL COMMENT '同组下id唯一',
    `group_name`    varchar(64)  NOT NULL COMMENT '组名称',
    `scene_name`    varchar(64)  NOT NULL COMMENT '场景名称',
    `idempotent_id` varchar(64)  NOT NULL COMMENT '幂等id',
    `biz_no`        varchar(64)  NOT NULL DEFAULT '' COMMENT '业务编号',
    `executor_name` varchar(512) NOT NULL DEFAULT '' COMMENT '执行器名称',
    `args_str`      text         NOT NULL COMMENT '执行方法参数',
    `ext_attrs`     text         NOT NULL COMMENT '扩展字段',
    `retry_status`  tinyint(4) NOT NULL DEFAULT '0' COMMENT '重试状态 0、失败 1、成功',
    `create_dt`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `error_message` text         NOT NULL COMMENT '异常信息',
    PRIMARY KEY (`id`),
    KEY             `idx_group_name_scene_name` (`group_name`, `scene_name`),
    KEY             `idx_retry_status` (`retry_status`),
    KEY             `idx_idempotent_id` (`idempotent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='重试日志表'
;

CREATE TABLE `scene_config`
(
    `id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `scene_name`       varchar(64)  NOT NULL COMMENT '场景名称',
    `group_name`       varchar(64)  NOT NULL COMMENT '组名称',
    `scene_status`     tinyint(4) NOT NULL DEFAULT '0' COMMENT '组状态 0、未启用 1、启用',
    `max_retry_count`  int(11) NOT NULL DEFAULT '5' COMMENT '最大重试次数',
    `back_off`         tinyint(4) NOT NULL DEFAULT '1' COMMENT '1、默认等级 2、固定间隔时间 3、CRON 表达式',
    `trigger_interval` varchar(16)  NOT NULL DEFAULT '' COMMENT '间隔时长',
    `deadline_request` bigint(20) unsigned NOT NULL DEFAULT '60000' COMMENT 'Deadline Request 调用链超时 单位毫秒',
    `description`      varchar(256) NOT NULL DEFAULT '' COMMENT '描述',
    `create_dt`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`scene_name`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='场景配置'
;

CREATE TABLE `server_node`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name`   varchar(64)  NOT NULL COMMENT '组名称',
    `host_id`      varchar(64)  NOT NULL COMMENT '主机id',
    `host_ip`      varchar(64)  NOT NULL COMMENT '机器ip',
    `context_path` varchar(256) NOT NULL DEFAULT '/' COMMENT '客户端上下文路径 server.servlet.context-path',
    `host_port`    int(16) NOT NULL COMMENT '机器端口',
    `expire_at`    datetime     NOT NULL COMMENT '过期时间',
    `node_type`    tinyint(4) NOT NULL COMMENT '节点类型 1、客户端 2、是服务端',
    `create_dt`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_host_id_host_ip` (`host_id`,`host_ip`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='服务器节点'
;

CREATE TABLE `shedlock`
(
    `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       varchar(64)  NOT NULL COMMENT '锁名称',
    `lock_until` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '锁定时长',
    `locked_at`  timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '锁定时间',
    `locked_by`  varchar(255) NOT NULL COMMENT '锁定者',
    `create_dt`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='锁定表'
;

CREATE TABLE `system_user`
(
    `id`        bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`  varchar(64)  NOT NULL COMMENT '账号',
    `password`  varchar(128) NOT NULL COMMENT '密码',
    `role`      tinyint(4) NOT NULL DEFAULT '0' COMMENT '角色：1-普通用户、2-管理员',
    `create_dt` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

INSERT INTO system_user (username, password, role)
VALUES ('admin', 'cdf4a007e2b02a0c49fc9b7ccfbb8a10c644f635e1765dcf2a7ab794ddc7edac', 2);

CREATE TABLE `system_user_permission`
(
    `id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name`     varchar(64) NOT NULL COMMENT '组名称',
    `system_user_id` bigint(20) NOT NULL COMMENT '系统用户id',
    `create_dt`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_name_system_user_id` (`group_name`, `system_user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户权限表';

CREATE TABLE `sequence_alloc`
(
    `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name` varchar(64) NOT NULL DEFAULT '' COMMENT '组名称',
    `max_id`     bigint(20) NOT NULL DEFAULT '1' COMMENT '最大id',
    `step`       int(11) NOT NULL DEFAULT '100' COMMENT '步长',
    `update_dt`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_name` (`group_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号段模式序号ID分配表';
