DROP
DATABASE IF EXISTS easy_retry;
CREATE
DATABASE easy_retry;
USE
easy_retry;
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
    `init_scene`        tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否初始化场景 0:否 1:是',
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
    `task_type`     tinyint(4) NOT NULL DEFAULT '1' COMMENT '任务类型 1、重试数据 2、回调数据',
    `create_dt`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY             `idx_group_name_scene_name` (`group_name`, `scene_name`),
    KEY             `idx_idempotent_id` (`idempotent_id`),
    KEY             `idx_biz_no` (`biz_no`),
    KEY             `idx_create_dt` (`create_dt`),
    UNIQUE KEY `uk_name_unique_id` (`group_name`, `unique_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='死信队列表'
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
    `task_type`       tinyint(4) NOT NULL DEFAULT '1' COMMENT '任务类型 1、重试数据 2、回调数据',
    `create_dt`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY               `idx_group_name_scene_name` (`group_name`, `scene_name`),
    KEY               `idx_retry_status` (`retry_status`),
    KEY               `idx_idempotent_id` (`idempotent_id`),
    KEY               `idx_biz_no` (`biz_no`),
    KEY               `idx_create_dt` (`create_dt`),
    UNIQUE KEY `uk_name_unique_id` (`group_name`, `unique_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='任务表'
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
    `retry_status`  tinyint(4) NOT NULL DEFAULT '0' COMMENT '重试状态 0、重试中 1、成功 2、最大次数',
    `task_type`     tinyint(4) NOT NULL DEFAULT '1' COMMENT '任务类型 1、重试数据 2、回调数据',
    `create_dt`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        PRIMARY KEY (`id`),
    KEY             `idx_group_name_scene_name` (`group_name`, `scene_name`),
    KEY             `idx_retry_status` (`retry_status`),
    KEY             `idx_idempotent_id` (`idempotent_id`),
    KEY             `idx_unique_id` (`unique_id`),
    KEY             `idx_biz_no` (`biz_no`),
    KEY             `idx_create_dt` (`create_dt`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='任务日志基础信息表'
;

CREATE TABLE `retry_task_log_message`
(
    `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name` varchar(64) NOT NULL COMMENT '组名称',
    `unique_id`  varchar(64) NOT NULL COMMENT '同组下id唯一',
    `create_dt`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `message`    text        NOT NULL COMMENT '异常信息',
    PRIMARY KEY (`id`),
    KEY          `idx_group_name_unique_id` (`group_name`, `unique_id`),
    KEY          `idx_create_dt` (`create_dt`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='任务调度日志信息记录表'
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
    `bucket_index` int(11) DEFAULT NULL COMMENT 'bucket',
    `description`      varchar(256) NOT NULL DEFAULT '' COMMENT '描述',
    `create_dt`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_name_scene_name` (`group_name`,`scene_name`)
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
    `ext_attrs`    varchar(256) NULL default '' COMMENT '扩展字段',
    `create_dt`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY            `idx_expire_at_node_type` (`expire_at`,`node_type`),
    UNIQUE KEY `uk_host_id_host_ip` (`host_id`,`host_ip`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='服务器节点'
;

CREATE TABLE `distributed_lock`
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

-- pwd: admin
INSERT INTO system_user (username, password, role)
VALUES ('admin', '465c194afb65670f38322df087f0a9bb225cc257e43eb4ac5a0c98ef5b3173ac', 2);

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

-- 分布式调度DDL

CREATE TABLE `job` (
    `id` BIGINT ( 20 ) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name` VARCHAR ( 64 ) NOT NULL COMMENT '组名称',
    `job_name` VARCHAR ( 64 ) NOT NULL COMMENT '名称',
    `args_str` TEXT NOT NULL COMMENT '执行方法参数',
    `args_type` VARCHAR ( 16 ) NOT NULL DEFAULT '' COMMENT '参数类型 text/json',
    `ext_attrs` TEXT NOT NULL COMMENT '扩展字段',
    `next_trigger_at` DATETIME NOT NULL COMMENT '下次触发时间',
    `job_status` TINYINT ( 4 ) NOT NULL DEFAULT '1' COMMENT '重试状态 0、关闭、1、开启',
    `route_key` VARCHAR ( 50 ) DEFAULT NULL COMMENT '执行器路由策略',
    `executor_type` TINYINT ( 4 ) NOT NULL DEFAULT '1' COMMENT '执行器类型 1、Java',
    `executor_name` VARCHAR ( 255 ) DEFAULT NULL COMMENT '执行器名称',
    `block_strategy` VARCHAR ( 50 ) DEFAULT NULL COMMENT '阻塞策略 1、丢弃 2、覆盖 3、并行',
    `executor_timeout` INT ( 11 ) NOT NULL DEFAULT '0' COMMENT '任务执行超时时间，单位秒',
    `max_retry_times` INT ( 11 ) NOT NULL DEFAULT '0' COMMENT '最大重试次数',
    `retry_interval` INT ( 11 ) NOT NULL DEFAULT '0' COMMENT '重试间隔(s)',
    `bucket_index` int(11) NOT NULL DEFAULT '0' COMMENT 'bucket',
    `description`      varchar(256) NOT NULL DEFAULT '' COMMENT '描述',
    `create_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted` TINYINT ( 4 ) NOT NULL DEFAULT '0' COMMENT '逻辑删除 1、删除',
    PRIMARY KEY ( `id` ),
    KEY `idx_group_name` ( `group_name` )
    ) ENGINE = INNODB AUTO_INCREMENT = 0 DEFAULT CHARSET = utf8mb4 COMMENT = '任务信息';

CREATE TABLE `job_task` (
    `id` BIGINT ( 20 ) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name` VARCHAR ( 64 ) NOT NULL COMMENT '组名称',
    `job_id` BIGINT ( 20 ) NOT NULL COMMENT '任务id',
    `retry_count` INT ( 11 ) NOT NULL DEFAULT '0' COMMENT '重试次数',
    `task_status` TINYINT ( 4 ) NOT NULL DEFAULT '0' COMMENT '任务状态 0、失败 1、成功',
    `create_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted` TINYINT ( 4 ) NOT NULL DEFAULT '0' COMMENT '逻辑删除 1、删除',
    PRIMARY KEY ( `id` )
    ) ENGINE = INNODB AUTO_INCREMENT = 0 DEFAULT CHARSET = utf8mb4 COMMENT = '调度任务';

CREATE TABLE `job_task_instance` (
    `id` BIGINT ( 20 ) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name` VARCHAR ( 64 ) NOT NULL COMMENT '组名称',
    `job_id` BIGINT ( 20 ) NOT NULL COMMENT '任务信息id',
    `task_id` BIGINT ( 20 ) NOT NULL COMMENT '调度任务id',
    `parent_id` BIGINT ( 20 ) NOT NULL DEFAULT '0' COMMENT '父执行器id',
    `execute_status` TINYINT ( 4 ) NOT NULL DEFAULT '0' COMMENT '执行的状态 0、失败 1、成功',
    `result_message` TEXT NOT NULL COMMENT '执行结果',
    `create_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY ( `id` )
    ) ENGINE = INNODB AUTO_INCREMENT = 0 DEFAULT CHARSET = utf8mb4 COMMENT = '任务实例';

CREATE TABLE `job_log_message` (
    `id` BIGINT ( 20 ) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_name` VARCHAR ( 64 ) NOT NULL COMMENT '组名称',
    `job_id` BIGINT ( 20 ) NOT NULL COMMENT '任务信息id',
    `task_id` BIGINT ( 20 ) NOT NULL COMMENT '任务实例id',
    `task_instance_id` BIGINT ( 20 ) NOT NULL COMMENT '调度任务id',
    `create_dt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `message` TEXT NOT NULL COMMENT '调度信息',
    PRIMARY KEY ( `id` )
    ) ENGINE = INNODB AUTO_INCREMENT = 0 DEFAULT CHARSET = utf8mb4 COMMENT = '调度日志';