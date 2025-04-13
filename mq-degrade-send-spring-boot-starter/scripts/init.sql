CREATE TABLE `mq_degrade_message_entity`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `resource`        varchar(255) NOT NULL COMMENT '资源名称',
    `message`         text COMMENT '发送消息内容',
    `msg_key`         varchar(255) DEFAULT NULL COMMENT '消息key',
    `context`         text COMMENT '上下文信息',
    `ip_addr`         varchar(64)  DEFAULT NULL COMMENT 'IP地址',
    `create_time`     datetime     NOT NULL COMMENT '创建时间',
    `retry_count`     int(11) DEFAULT '0' COMMENT '重试次数',
    `last_retry_time` datetime     DEFAULT NULL COMMENT '最新重试时间',
    PRIMARY KEY (`id`),
    KEY               `idx_resource` (`resource`),
    KEY               `idx_msg_key` (`msg_key`),
    KEY               `idx_ip_retry_last_retry` (`ip_addr`, `retry_count`, `last_retry_time`),
    KEY               `idx_retry_last_retry` (`retry_count`, `last_retry_time`),
    KEY               `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ降级消息实体表';