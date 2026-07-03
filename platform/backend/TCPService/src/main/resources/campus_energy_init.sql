-- 校园能源管理系统数据库初始化脚本

-- 校园区域表
CREATE TABLE IF NOT EXISTS campus_area (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '区域ID',
    area_name VARCHAR(100) NOT NULL COMMENT '区域名称',
    area_type VARCHAR(50) COMMENT '区域类型(教学区/宿舍区/办公区/运动区/景观区)',
    area_size DECIMAL(10, 2) COMMENT '区域面积(平方米)',
    lamp_count INT DEFAULT 0 COMMENT '路灯数量',
    device_count INT DEFAULT 0 COMMENT '设备数量',
    manager_name VARCHAR(50) COMMENT '负责人姓名',
    manager_phone VARCHAR(20) COMMENT '负责人电话',
    status INT DEFAULT 1 COMMENT '状态(1-启用, 0-禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    monthly_energy_consumption DECIMAL(10, 2) DEFAULT 0 COMMENT '月度能耗(kWh)',
    monthly_energy_saved DECIMAL(10, 2) DEFAULT 0 COMMENT '月度节能(kWh)',
    saving_rate DECIMAL(5, 2) DEFAULT 0 COMMENT '节能率(%)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园区域表';

-- 能耗账单表
CREATE TABLE IF NOT EXISTS energy_bill (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '账单ID',
    area_id INT COMMENT '区域ID',
    area_name VARCHAR(100) COMMENT '区域名称',
    bill_month VARCHAR(7) NOT NULL COMMENT '账单月份(yyyy-MM)',
    total_consumption DECIMAL(10, 2) DEFAULT 0 COMMENT '总能耗(kWh)',
    total_cost DECIMAL(10, 2) DEFAULT 0 COMMENT '总费用(元)',
    saved_consumption DECIMAL(10, 2) DEFAULT 0 COMMENT '节约能耗(kWh)',
    saved_cost DECIMAL(10, 2) DEFAULT 0 COMMENT '节约费用(元)',
    saving_rate DECIMAL(5, 2) DEFAULT 0 COMMENT '节能率(%)',
    peak_consumption DECIMAL(10, 2) DEFAULT 0 COMMENT '高峰期能耗(kWh)',
    off_peak_consumption DECIMAL(10, 2) DEFAULT 0 COMMENT '低谷期能耗(kWh)',
    average_daily_consumption DECIMAL(10, 2) DEFAULT 0 COMMENT '日均能耗(kWh)',
    report_url VARCHAR(255) COMMENT '报表文件URL',
    status INT DEFAULT 1 COMMENT '状态(1-已生成, 0-待确认)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    waste_analysis TEXT COMMENT '浪费分析',
    optimization_suggestions TEXT COMMENT '优化建议',
    INDEX idx_area_id (area_id),
    INDEX idx_bill_month (bill_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能耗账单表';

-- 教学案例表
CREATE TABLE IF NOT EXISTS teaching_case (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '案例ID',
    case_name VARCHAR(200) NOT NULL COMMENT '案例名称',
    case_type VARCHAR(50) COMMENT '案例类型(lighting_control/environment_monitor/energy_saving)',
    description VARCHAR(500) COMMENT '案例描述',
    device_id VARCHAR(50) COMMENT '关联设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    data_source VARCHAR(100) COMMENT '数据来源',
    teaching_content TEXT COMMENT '教学内容',
    experiment_guide TEXT COMMENT '实验指导',
    related_course VARCHAR(200) COMMENT '相关课程',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    status INT DEFAULT 1 COMMENT '状态(1-启用, 0-禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    data_preview TEXT COMMENT '数据预览(JSON格式)',
    analysis_result TEXT COMMENT '分析结果',
    INDEX idx_case_type (case_type),
    INDEX idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学案例表';

-- 插入示例校园区域数据
INSERT INTO campus_area (area_name, area_type, area_size, lamp_count, device_count, manager_name, manager_phone) VALUES
('教学楼A区', '教学区', 5000.00, 45, 12, '张三', '13800138001'),
('教学楼B区', '教学区', 4500.00, 38, 10, '李四', '13800138002'),
('学生宿舍1号楼', '宿舍区', 3000.00, 30, 8, '王五', '13800138003'),
('学生宿舍2号楼', '宿舍区', 3200.00, 32, 8, '赵六', '13800138004'),
('图书馆', '办公区', 8000.00, 60, 15, '钱七', '13800138005'),
('体育馆', '运动区', 6000.00, 50, 10, '孙八', '13800138006'),
('校园主干道', '景观区', 2000.00, 80, 5, '周九', '13800138007'),
('中心花园', '景观区', 1500.00, 25, 3, '吴十', '13800138008');

-- 插入示例教学案例数据
INSERT INTO teaching_case (case_name, case_type, description, device_id, device_name, data_source, related_course, status) VALUES
('智能路灯控制基础实验', 'lighting_control', '基于光照传感器的智能路灯控制实验', 'LAMP001', '教学楼A区-路灯01', '实时设备数据', '物联网技术基础', 1),
('环境监测数据采集实验', 'environment_monitor', '温湿度、光照数据采集与分析实验', 'ENV001', '教学楼A区-环境监测01', '实时设备数据', '传感器技术', 1),
('智能节能控制实验', 'energy_saving', '基于AI算法的智能节能控制实验', 'LAMP002', '图书馆-路灯01', '实时设备数据', '智能控制', 1);
