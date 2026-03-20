INSERT INTO device_type (name, type, sensors, device_count, status, color, description) VALUES 
('温湿度传感器', 'temperature_humidity', '["温度", "湿度"]', 15, 'active', '#409EFF', '监测环境温度和湿度'),
('光照传感器', 'light', '["光照强度"]', 8, 'active', '#E6A23C', '监测环境光照强度'),
('土壤传感器', 'soil', '["土壤湿度", "土壤温度"]', 12, 'active', '#67C23A', '监测土壤湿度和温度'),
('空气质量传感器', 'air', '["PM2.5", "PM10", "CO2"]', 5, 'active', '#F56C6C', '监测空气质量指标'),
('风速传感器', 'wind', '["风速", "风向"]', 3, 'active', '#909399', '监测风速和风向'),
('综合环境监测站', 'comprehensive', '["温度", "湿度", "光照", "土壤湿度", "土壤温度"]', 2, 'active', '#E0A800', '综合监测多种环境参数');

INSERT INTO monitor_record (device_id, device_name, record_time, data_type, value, status, remark) VALUES
('ENV001', '农田监测点1号', NOW() - INTERVAL 1 HOUR, 'temperature', 26.5, 'normal', '正常'),
('ENV001', '农田监测点1号', NOW() - INTERVAL 1 HOUR, 'humidity', 65.0, 'normal', '正常'),
('ENV002', '温室大棚A区', NOW() - INTERVAL 2 HOUR, 'temperature', 32.5, 'warning', '温度偏高'),
('ENV002', '温室大棚A区', NOW() - INTERVAL 2 HOUR, 'humidity', 85.0, 'warning', '湿度过高'),
('ENV003', '果园监测站', NOW() - INTERVAL 3 HOUR, 'light', 15000.0, 'normal', '正常'),
('ENV001', '农田监测点1号', NOW() - INTERVAL 4 HOUR, 'temperature', 28.0, 'normal', '正常'),
('ENV002', '温室大棚A区', NOW() - INTERVAL 5 HOUR, 'temperature', 35.0, 'abnormal', '温度过高'),
('ENV003', '果园监测站', NOW() - INTERVAL 6 HOUR, 'humidity', 45.0, 'warning', '湿度偏低');

UPDATE user SET create_time = '2025-01-01 10:00:00', last_login_time = NOW() WHERE username = 'admin';
UPDATE user SET create_time = '2025-01-15 14:30:00' WHERE username = 'test';
