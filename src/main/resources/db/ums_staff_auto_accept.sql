-- 服务人员自动接单开关（用于自动派单功能）
-- 执行前请确认 ums_staff 表已存在；若该列已存在请跳过

ALTER TABLE ums_staff
  ADD COLUMN auto_accept tinyint NOT NULL DEFAULT 0 COMMENT '是否自动接单: 0-否, 1-是';
