CREATE DATABASE IF NOT EXISTS light DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE light;

CREATE TABLE IF NOT EXISTS user (
    userId INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    userPermission INT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS device (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    ip VARCHAR(50),
    port INT,
    deviceType VARCHAR(50),
    temperature FLOAT,
    humidity INT,
    lightIntensity INT,
    onLine INT DEFAULT 0,
    isBusy INT DEFAULT 0,
    isBroken INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO user (username, password, userPermission) VALUES 
('admin', 'admin123', 3),
('test', 'test123', 1);

INSERT INTO device (id, name, ip, port, deviceType, temperature, humidity, lightIntensity, onLine, isBusy, isBroken) VALUES 
('DEV001', '农田监测点1号', '192.168.1.101', 8001, '环境传感器', 25.6, 68, 12500, 1, 0, 0),
('DEV002', '温室大棚A区', '192.168.1.102', 8002, '环境传感器', 28.3, 75, 8000, 1, 0, 0),
('DEV003', '果园监测点', '192.168.1.103', 8003, '环境传感器', 24.1, 62, 15000, 0, 0, 0),
('DEV004', '蔬菜大棚B区', '192.168.1.104', 8004, '环境传感器', 26.8, 70, 9500, 1, 0, 0),
('DEV005', '花卉温室', '192.168.1.105', 8005, '环境传感器', 23.5, 80, 7000, 1, 0, 0);
