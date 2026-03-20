-- 智慧环境监测系统 - 数据库初始化脚本

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS light DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE light;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    userId INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    userPermission INT DEFAULT 1 COMMENT '用户权限: 1-普通用户, 2-管理员, 3-超级管理员'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建设备表
CREATE TABLE IF NOT EXISTS device (
    id VARCHAR(50) PRIMARY KEY COMMENT '设备ID',
    name VARCHAR(100) NOT NULL COMMENT '设备名称',
    ip VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口',
    deviceType VARCHAR(50) COMMENT '设备类型',
    temperature FLOAT COMMENT '温度(°C)',
    humidity INT COMMENT '湿度(%)',
    lightIntensity INT COMMENT '光照强度(lux)',
    onLine INT DEFAULT 0 COMMENT '在线状态: 0-离线, 1-在线',
    isBusy INT DEFAULT 0 COMMENT '是否繁忙: 0-否, 1-是',
    isBroken INT DEFAULT 0 COMMENT '是否损坏: 0-否, 1-是'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 插入默认管理员用户
INSERT INTO user (username, password, userPermission) VALUES 
('admin', 'admin123', 3),
('test', 'test123', 1);

-- 插入测试设备数据
INSERT INTO device (id, name, ip, port, deviceType, temperature, humidity, lightIntensity, onLine, isBusy, isBroken) VALUES 
('DEV001', '农田监测点1号', '192.168.1.101', 8001, '环境传感器', 25.6, 68, 12500, 1, 0, 0),
('DEV002', '温室大棚A区', '192.168.1.102', 8002, '环境传感器', 28.3, 75, 8000, 1, 0, 0),
('DEV003', '果园监测点', '192.168.1.103', 8003, '环境传感器', 24.1, 62, 15000, 0, 0, 0),
('DEV004', '蔬菜大棚B区', '192.168.1.104', 8004, '环境传感器', 26.8, 70, 9500, 1, 0, 0),
('DEV005', '花卉温室', '192.168.1.105', 8005, '环境传感器', 23.5, 80, 7000, 1, 0, 0);

-- 查询验证
SELECT '用户表数据:' AS info;
SELECT * FROM user;

SELECT '设备表数据:' AS info;
SELECT * FROM device;
