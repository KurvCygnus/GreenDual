-- 智绿云控 - AI协同的环境监测与智能路灯节能平台
-- 初始化用户数据

USE envsense;

-- 插入测试用户
INSERT INTO user (username, password, userPermission) VALUES
('admin', 'admin123', 3),  -- 系统管理员
('operator', 'operator123', 2),  -- 运维工程师
('viewer', 'viewer123', 1);  -- 观察员

-- 验证插入的数据
SELECT * FROM user;
