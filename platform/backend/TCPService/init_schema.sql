-- ============================================================
-- 智慧环境监测系统 · 数据库架构初始化脚本
-- 数据库: light (对应 application.properties: jdbc:mysql://localhost:3306/light)
-- 字符集: utf8mb4 · 引擎: InnoDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS light
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE light;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS user (
    userId          INT             PRIMARY KEY AUTO_INCREMENT  COMMENT '用户ID',
    username        VARCHAR(50)     NOT NULL UNIQUE             COMMENT '用户名',
    password        VARCHAR(100)    NOT NULL                    COMMENT '密码',
    userPermission  INT             DEFAULT 1                   COMMENT '权限等级: 1-观察员, 2-运维工程师, 3-系统管理员',
    create_time     DATETIME                                    COMMENT '创建时间',
    last_login_time DATETIME                                    COMMENT '最后登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 设备表（TCP 设备 + 路灯共用）
-- ============================================================
CREATE TABLE IF NOT EXISTS device (
    id                VARCHAR(50)   PRIMARY KEY             COMMENT '设备ID',
    name              VARCHAR(100)  NOT NULL                COMMENT '设备名称',
    ip                VARCHAR(50)                           COMMENT 'IP地址',
    port              INT                                   COMMENT '端口',
    deviceType        VARCHAR(50)                           COMMENT '设备类型',
    temperature       FLOAT                                 COMMENT '温度(°C)',
    humidity          INT                                   COMMENT '湿度(%)',
    lightIntensity    INT                                   COMMENT '光照强度(lux)',
    onLine            INT           DEFAULT 0               COMMENT '在线状态: 0-离线, 1-在线',
    isBusy            INT           DEFAULT 0               COMMENT '是否繁忙: 0-否, 1-是',
    isBroken          INT           DEFAULT 0               COMMENT '是否损坏: 0-否, 1-是',
    -- 路灯专用字段
    lampStatus        INT           DEFAULT 0               COMMENT '路灯开关: 0-关, 1-开',
    brightness        INT           DEFAULT 0               COMMENT '亮度 0-100',
    longitude         DOUBLE                                COMMENT '经度',
    latitude          DOUBLE                                COMMENT '纬度',
    powerConsumption  FLOAT                                 COMMENT '功耗(W)',
    lastMaintenance   VARCHAR(20)                           COMMENT '上次维护日期',
    faultStatus       INT           DEFAULT 0               COMMENT '故障状态: 0-正常, 1-故障'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- ============================================================
-- 3. 设备类型表
-- ============================================================
CREATE TABLE IF NOT EXISTS device_type (
    id            INT           PRIMARY KEY AUTO_INCREMENT  COMMENT '类型ID',
    name          VARCHAR(100)                              COMMENT '类型名称',
    type          VARCHAR(100)                              COMMENT '类型标识',
    sensors       VARCHAR(500)                              COMMENT '传感器列表(JSON数组)',
    device_count  INT                                       COMMENT '设备数量',
    status        VARCHAR(20)                               COMMENT '状态',
    color         VARCHAR(20)                               COMMENT '标识颜色',
    description   TEXT                                      COMMENT '描述',
    created_at    DATETIME                                  COMMENT '创建时间',
    updated_at    DATETIME                                  COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备类型表';

-- ============================================================
-- 4. 监测记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS monitor_record (
    id          INT           PRIMARY KEY AUTO_INCREMENT  COMMENT '记录ID',
    device_id   VARCHAR(50)                               COMMENT '设备ID',
    device_name VARCHAR(100)                              COMMENT '设备名称',
    record_time DATETIME                                  COMMENT '记录时间',
    data_type   VARCHAR(50)                               COMMENT '数据类型(temperature/humidity/light等)',
    value       DOUBLE                                    COMMENT '数值',
    status      VARCHAR(20)                               COMMENT '状态(normal/warning/abnormal)',
    remark      VARCHAR(500)                              COMMENT '备注',
    created_at  DATETIME                                  COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测记录表';

-- ============================================================
-- 5. 维修日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS MaintenanceLog (
    id        INT           PRIMARY KEY AUTO_INCREMENT  COMMENT '日志ID',
    time      DATETIME                                  COMMENT '记录时间',
    subject   VARCHAR(200)                              COMMENT '主题',
    content   TEXT                                      COMMENT '内容',
    userId    INT                                       COMMENT '操作用户ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修日志表';

-- ============================================================
-- 6. AI 员工表
-- ============================================================
CREATE TABLE IF NOT EXISTS ai_employee (
    ai_id            BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT 'AI员工ID',
    name             VARCHAR(100)                              COMMENT '名称',
    job_description  TEXT                                      COMMENT '工作描述',
    work_start_time  TIME                                      COMMENT '上班时间',
    work_end_time    TIME                                      COMMENT '下班时间',
    daily_summary    TEXT                                      COMMENT '工作总结',
    user_id          BIGINT                                    COMMENT '关联用户ID',
    is_working       INT           DEFAULT 0                   COMMENT '是否工作中: 0-否, 1-是'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI员工表';

-- ============================================================
-- 7. AI 行动记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS ai_action_log (
    action_id          BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '行动ID',
    ai_id              BIGINT                                   COMMENT 'AI员工ID',
    action_description TEXT                                      COMMENT '行动描述',
    action_time        DATETIME                                 COMMENT '行动时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI行动记录表';

-- ============================================================
-- 8. 校园区域表
-- ============================================================
CREATE TABLE IF NOT EXISTS campus_area (
    id                         INT           PRIMARY KEY AUTO_INCREMENT  COMMENT '区域ID',
    area_name                  VARCHAR(100)  NOT NULL                    COMMENT '区域名称',
    area_type                  VARCHAR(50)                               COMMENT '区域类型(教学区/宿舍区/办公区/运动区/景观区)',
    area_size                  DECIMAL(10,2)                             COMMENT '面积(平方米)',
    lamp_count                 INT           DEFAULT 0                   COMMENT '路灯数量',
    device_count               INT           DEFAULT 0                   COMMENT '设备数量',
    manager_name               VARCHAR(50)                               COMMENT '负责人',
    manager_phone              VARCHAR(20)                               COMMENT '负责人电话',
    status                     INT           DEFAULT 1                   COMMENT '状态: 1-启用, 0-禁用',
    create_time                DATETIME      DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    update_time                DATETIME      DEFAULT CURRENT_TIMESTAMP
                                              ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    monthly_energy_consumption DECIMAL(10,2) DEFAULT 0                   COMMENT '月度能耗(kWh)',
    monthly_energy_saved       DECIMAL(10,2) DEFAULT 0                   COMMENT '月度节能(kWh)',
    saving_rate                DECIMAL(5,2)  DEFAULT 0                   COMMENT '节能率(%)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园区域表';

-- ============================================================
-- 9. 能耗账单表
-- ============================================================
CREATE TABLE IF NOT EXISTS energy_bill (
    id                        INT           PRIMARY KEY AUTO_INCREMENT  COMMENT '账单ID',
    area_id                   INT                                       COMMENT '区域ID',
    area_name                 VARCHAR(100)                              COMMENT '区域名称',
    bill_month                VARCHAR(7)    NOT NULL                    COMMENT '账单月份(yyyy-MM)',
    total_consumption         DECIMAL(10,2) DEFAULT 0                   COMMENT '总能耗(kWh)',
    total_cost                DECIMAL(10,2) DEFAULT 0                   COMMENT '总费用(元)',
    saved_consumption         DECIMAL(10,2) DEFAULT 0                   COMMENT '节约能耗(kWh)',
    saved_cost                DECIMAL(10,2) DEFAULT 0                   COMMENT '节约费用(元)',
    saving_rate               DECIMAL(5,2)  DEFAULT 0                   COMMENT '节能率(%)',
    peak_consumption          DECIMAL(10,2) DEFAULT 0                   COMMENT '高峰期能耗(kWh)',
    off_peak_consumption      DECIMAL(10,2) DEFAULT 0                   COMMENT '低谷期能耗(kWh)',
    average_daily_consumption DECIMAL(10,2) DEFAULT 0                   COMMENT '日均能耗(kWh)',
    report_url                VARCHAR(255)                              COMMENT '报表文件URL',
    status                    INT           DEFAULT 1                   COMMENT '状态: 1-已生成, 0-待确认',
    create_time               DATETIME      DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    waste_analysis            TEXT                                      COMMENT '浪费分析',
    optimization_suggestions  TEXT                                      COMMENT '优化建议',
    INDEX idx_area_id    (area_id),
    INDEX idx_bill_month (bill_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='能耗账单表';

-- ============================================================
-- 10. 教学案例表
-- ============================================================
CREATE TABLE IF NOT EXISTS teaching_case (
    id                INT           PRIMARY KEY AUTO_INCREMENT  COMMENT '案例ID',
    case_name         VARCHAR(200)  NOT NULL                    COMMENT '案例名称',
    case_type         VARCHAR(50)                               COMMENT '案例类型(lighting_control/environment_monitor/energy_saving)',
    description       VARCHAR(500)                              COMMENT '案例描述',
    device_id         VARCHAR(50)                               COMMENT '关联设备ID',
    device_name       VARCHAR(100)                              COMMENT '设备名称',
    data_source       VARCHAR(100)                              COMMENT '数据来源',
    teaching_content  TEXT                                      COMMENT '教学内容',
    experiment_guide  TEXT                                      COMMENT '实验指导',
    related_course    VARCHAR(200)                              COMMENT '相关课程',
    use_count         INT           DEFAULT 0                   COMMENT '使用次数',
    status            INT           DEFAULT 1                   COMMENT '状态: 1-启用, 0-禁用',
    create_time       DATETIME      DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    update_time       DATETIME      DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    data_preview      TEXT                                      COMMENT '数据预览(JSON)',
    analysis_result   TEXT                                      COMMENT '分析结果',
    INDEX idx_case_type (case_type),
    INDEX idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学案例表';