-- 管理员扩展表（区分超级管理员与普通管理员）
-- 超级管理员：admin_type=1，created_by 为 NULL（系统初始化）
-- 普通管理员：admin_type=0，由超级管理员创建，拥有余额
CREATE TABLE IF NOT EXISTS `sys_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` bigint NOT NULL COMMENT '关联sys_user',
  `admin_type` tinyint NOT NULL COMMENT '管理员类型: 0-普通管理员, 1-超级管理员',
  `balance` decimal(10,2) DEFAULT 0.00 COMMENT '账户余额(普通管理员使用)',
  `created_by` bigint COMMENT '创建者user_id，超级管理员为NULL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_admin_type` (`admin_type`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员扩展表';
