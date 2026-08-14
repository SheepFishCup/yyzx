-- ============================================================
-- Seata AT 模式 undo_log 表（在共享数据库 yyzx 中执行一次）
-- 用法: mysql -u root -p yyzx < scripts/seata-undo-log.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `undo_log` (
    `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `branch_id`     BIGINT(20)   NOT NULL COMMENT '分支事务ID',
    `xid`           VARCHAR(100) NOT NULL COMMENT '全局事务ID',
    `context`       VARCHAR(128) NOT NULL COMMENT '上下文信息',
    `rollback_info` LONGBLOB     NOT NULL COMMENT '回滚信息(undo log)',
    `log_status`    INT(11)      NOT NULL COMMENT '状态:0正常 1全局完成',
    `log_created`   DATETIME     NOT NULL COMMENT '创建时间',
    `log_modified`  DATETIME     NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='Seata undo_log 表';
