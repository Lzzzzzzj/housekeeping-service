-- 服务人员申请表
-- 用户申请成为服务人员后，管理员审核通过则创建 ums_staff 并更新 sys_user.user_type
CREATE TABLE IF NOT EXISTS `ums_staff_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` bigint NOT NULL COMMENT '申请人ID(关联sys_user)',
  `real_name` varchar(32) NOT NULL COMMENT '真实姓名',
  `id_card` varchar(20) COMMENT '身份证号',
  `phone` varchar(20) COMMENT '联系电话',
  `health_cert_url` varchar(255) COMMENT '健康证图片地址',
  `skill_cert_urls` json COMMENT '技能证书图片(JSON数组)',
  `apply_reason` varchar(500) COMMENT '申请理由',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已通过, 2-驳回',
  `reject_reason` varchar(255) COMMENT '驳回原因',
  `audit_time` datetime COMMENT '审核时间',
  `audit_user_id` bigint COMMENT '审核人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务人员申请表';
