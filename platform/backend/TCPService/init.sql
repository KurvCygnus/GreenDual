-- 智慧环境监测系统 - 数据库初始化脚本

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS envsense DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE envsense;

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
    isBroken INT DEFAULT 0 COMMENT '是否损坏: 0-否, 1-是',
    -- 路灯相关字段
    lampStatus INT DEFAULT 0 COMMENT '路灯开关状态: 0-关, 1-开',
    brightness INT DEFAULT 0 COMMENT '亮度: 0-100',
    longitude DOUBLE COMMENT '经度',
    latitude DOUBLE COMMENT '纬度',
    powerConsumption FLOAT COMMENT '功耗(W)',
    lastMaintenance VARCHAR(20) COMMENT '上次维护日期',
    faultStatus INT DEFAULT 0 COMMENT '故障状态: 0-正常, 1-故障'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 创建监测记录表
CREATE TABLE IF NOT EXISTS monitor_record (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    deviceId VARCHAR(50) COMMENT '设备ID',
    recordTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    temperature FLOAT COMMENT '温度',
    humidity INT COMMENT '湿度',
    lightIntensity INT COMMENT '光照强度',
    status VARCHAR(20) COMMENT '状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测记录表';

-- 插入默认管理员用户
INSERT INTO user (username, password, userPermission) VALUES 
('admin', 'admin123', 3),
('test', 'test123', 1);

-- 插入测试设备数据 - 环境传感器
INSERT INTO device (id, name, ip, port, deviceType, temperature, humidity, lightIntensity, onLine, isBusy, isBroken) VALUES 
('DEV001', '农田监测点1号', '192.168.1.101', 8001, '环境传感器', 25.6, 68, 12500, 1, 0, 0),
('DEV002', '温室大棚A区', '192.168.1.102', 8002, '环境传感器', 28.3, 75, 8000, 1, 0, 0),
('DEV003', '果园监测点', '192.168.1.103', 8003, '环境传感器', 24.1, 62, 15000, 0, 0, 0);

-- 插入测试路灯数据 - 共30盏，分布在教学区、宿舍区、办公区、运动区
INSERT INTO device (id, name, ip, port, deviceType, onLine, isBusy, isBroken, lampStatus, brightness, longitude, latitude, powerConsumption, lastMaintenance, faultStatus) VALUES 
-- 教学区路灯 (12盏)
('LAMP001', '教学区-主干道-01', '192.168.2.101', 9001, '路灯', 1, 0, 0, 1, 80, 116.397428, 39.90923, 80, '2024-01-15', 0),
('LAMP002', '教学区-主干道-02', '192.168.2.102', 9002, '路灯', 1, 0, 0, 1, 75, 116.397528, 39.90933, 80, '2024-01-15', 0),
('LAMP003', '教学区-主干道-03', '192.168.2.103', 9003, '路灯', 1, 0, 0, 1, 85, 116.397628, 39.90943, 80, '2024-02-01', 0),
('LAMP004', '教学区-教学楼A-01', '192.168.2.104', 9004, '路灯', 1, 0, 0, 1, 90, 116.397728, 39.90953, 80, '2024-01-20', 0),
('LAMP005', '教学区-教学楼A-02', '192.168.2.105', 9005, '路灯', 1, 0, 0, 1, 88, 116.397828, 39.90963, 80, '2024-01-20', 0),
('LAMP006', '教学区-教学楼B-01', '192.168.2.106', 9006, '路灯', 1, 0, 0, 1, 82, 116.397928, 39.90973, 80, '2024-02-10', 0),
('LAMP007', '教学区-教学楼B-02', '192.168.2.107', 9007, '路灯', 1, 0, 0, 1, 78, 116.398028, 39.90983, 80, '2024-02-10', 0),
('LAMP008', '教学区-实验楼-01', '192.168.2.108', 9008, '路灯', 1, 0, 0, 1, 85, 116.398128, 39.90993, 80, '2024-01-25', 0),
('LAMP009', '教学区-实验楼-02', '192.168.2.109', 9009, '路灯', 1, 0, 0, 1, 80, 116.398228, 39.91003, 80, '2024-01-25', 0),
('LAMP010', '教学区-图书馆-01', '192.168.2.110', 9010, '路灯', 1, 0, 0, 1, 75, 116.398328, 39.91013, 80, '2024-02-05', 0),
('LAMP011', '教学区-图书馆-02', '192.168.2.111', 9011, '路灯', 1, 0, 0, 1, 77, 116.398428, 39.91023, 80, '2024-02-05', 0),
('LAMP012', '教学区-广场-01', '192.168.2.112', 9012, '路灯', 1, 0, 0, 1, 90, 116.398528, 39.91033, 80, '2024-01-30', 0),
-- 宿舍区路灯 (8盏)
('LAMP013', '宿舍区-1号楼-01', '192.168.2.113', 9013, '路灯', 1, 0, 0, 1, 70, 116.398628, 39.91043, 80, '2024-02-10', 0),
('LAMP014', '宿舍区-1号楼-02', '192.168.2.114', 9014, '路灯', 1, 0, 0, 1, 72, 116.398728, 39.91053, 80, '2024-02-10', 0),
('LAMP015', '宿舍区-2号楼-01', '192.168.2.115', 9015, '路灯', 1, 0, 0, 1, 68, 116.398828, 39.91063, 80, '2024-02-12', 0),
('LAMP016', '宿舍区-2号楼-02', '192.168.2.116', 9016, '路灯', 1, 0, 0, 1, 65, 116.398928, 39.91073, 80, '2024-02-12', 0),
('LAMP017', '宿舍区-3号楼-01', '192.168.2.117', 9017, '路灯', 1, 0, 0, 1, 75, 116.399028, 39.91083, 80, '2024-02-15', 0),
('LAMP018', '宿舍区-3号楼-02', '192.168.2.118', 9018, '路灯', 1, 0, 0, 1, 73, 116.399128, 39.91093, 80, '2024-02-15', 0),
('LAMP019', '宿舍区-主干道-01', '192.168.2.119', 9019, '路灯', 1, 0, 0, 1, 80, 116.399228, 39.91103, 80, '2024-02-08', 0),
('LAMP020', '宿舍区-主干道-02', '192.168.2.120', 9020, '路灯', 1, 0, 0, 1, 78, 116.399328, 39.91113, 80, '2024-02-08', 0),
-- 办公区路灯 (6盏)
('LAMP021', '办公区-行政楼-01', '192.168.2.121', 9021, '路灯', 1, 0, 0, 1, 85, 116.399428, 39.91123, 80, '2024-01-18', 0),
('LAMP022', '办公区-行政楼-02', '192.168.2.122', 9022, '路灯', 1, 0, 0, 1, 82, 116.399528, 39.91133, 80, '2024-01-18', 0),
('LAMP023', '办公区-停车场-01', '192.168.2.123', 9023, '路灯', 1, 0, 0, 1, 90, 116.399628, 39.91143, 80, '2024-01-22', 0),
('LAMP024', '办公区-停车场-02', '192.168.2.124', 9024, '路灯', 1, 0, 0, 1, 88, 116.399728, 39.91153, 80, '2024-01-22', 0),
('LAMP025', '办公区-大门-01', '192.168.2.125', 9025, '路灯', 1, 0, 0, 1, 95, 116.399828, 39.91163, 80, '2024-01-28', 0),
('LAMP026', '办公区-侧门-01', '192.168.2.126', 9026, '路灯', 1, 0, 0, 1, 80, 116.399928, 39.91173, 80, '2024-01-28', 0),
-- 运动区路灯 (4盏)
('LAMP027', '运动区-操场-01', '192.168.2.127', 9027, '路灯', 1, 0, 0, 1, 100, 116.400028, 39.91183, 80, '2024-01-25', 0),
('LAMP028', '运动区-操场-02', '192.168.2.128', 9028, '路灯', 1, 0, 0, 1, 100, 116.400128, 39.91193, 80, '2024-01-25', 0),
('LAMP029', '运动区-体育馆-01', '192.168.2.129', 9029, '路灯', 1, 0, 0, 1, 85, 116.400228, 39.91203, 80, '2024-02-01', 0),
('LAMP030', '运动区-体育馆-02', '192.168.2.130', 9030, '路灯', 1, 0, 0, 1, 88, 116.400328, 39.91213, 80, '2024-02-01', 0);

-- 查询验证
SELECT '用户表数据:' AS info;
SELECT * FROM user;

SELECT '设备表数据:' AS info;
SELECT * FROM device;

SELECT '路灯设备:' AS info;
SELECT * FROM device WHERE deviceType = '路灯';
