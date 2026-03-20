# � 智绿云控 · AI协同的环境监测与智能路灯节能平台

> 基于 **HarmonyOS + Spring Boot + Vue3** 的物联网环境监测与智能路灯管理平台，实现环境数据实时监测、路灯远程控制与集中运维。

---

## 📖 项目简介

本项目面向智慧城市场景，采用 **分层架构设计**，实现从嵌入式设备端到云端、再到 Web 管理端的完整闭环：

- **嵌入式设备层**：HarmonyOS + BearPi-HM Nano（Hi3861）
- **后端平台层**：Spring Boot + TCP Socket 通信 + MyBatis
- **前端管理层**：Vue3 + Element Plus

**核心功能**：

- 🌡️ 环境监测：温度、湿度、光照强度实时采集
- 💡 智能路灯：远程开关、亮度调节、故障检测
- 📊 数据可视化：实时数据展示、历史趋势分析
- � 智能告警：异常数据自动报警
- 🌐 设备管理：设备注册、状态监控、固件版本管理

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                   前端展示层 (Vue3 + Element Plus)              │
│                   platform/env-monitor-ui/                   │
└──────────────────────────┬──────────────────────────────────┘
                           │ RESTful API
┌──────────────────────────▼──────────────────────────────────┐
│                  后端服务层 (Spring Boot)                     │
│              platform/env-springboot/TCPService/             │
│              - TCP Server: 设备通信与数据接收                  │
│              - REST API: 前端数据接口                         │
│              - MySQL: 数据持久化                              │
└──────────────────────────┬──────────────────────────────────┘
                           │ TCP Socket
┌──────────────────────────▼──────────────────────────────────┐
│              嵌入式设备层 (HarmonyOS + BearPi-HM Nano)         │
│           flushbonading/this/applications/BearPi/...          │
│              - E53_IA1传感器: 温湿度、光照采集                 │
│              - E53_SC1传感器: 光照强度采集                     │
│              - TCP Client: 与平台通信                         │
└──────────────────────────┬──────────────────────────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
        ┌───────────┐             ┌───────────┐
        │ 环境监测模块 │             │ 路灯控制模块 │
        │温湿度/光照  │             │开关/亮度   │
        └───────────┘             └───────────┘
```

---

## 📂 目录结构

```
EnvSense/
├── flushbonading/this/                    # HarmonyOS 嵌入式工程
│   ├── applications/BearPi/BearPi-HM_Nano/sample/
│   │   ├── D3_iot_udp_client/            # 主程序 - TCP客户端
│   │   │   ├── udp_client_demo.c         # TCP连接与主循环
│   │   │   ├── data_processing.c         # 数据处理与命令解析
│   │   │   └── string_tool.c             # 字符串工具函数
│   │   ├── C2_e53_ia1_temp_humi_pls/     # E53_IA1传感器驱动(温湿度+光照)
│   │   └── C3_e53_sc1_pls/               # E53_SC1传感器驱动(光照)
│   ├── lib/custom_tool/                  # 工具库
│   │   ├── cJSON.h/cJSON.c               # JSON解析库
│   │   └── customTool.h                  # 工具头文件
│   └── global_settings.h                 # 全局配置文件
│
├── platform/
│   ├── env-monitor-ui/                   # Vue2 前端项目
│   │   ├── src/
│   │   │   ├── views/                    # 页面组件
│   │   │   ├── components/               # 公共组件
│   │   │   └── api/                      # API接口
│   │   └── package.json
│   │
│   └── env-springboot/                   # Spring Boot 后端项目
│       └── TCPService/
│           ├── src/main/java/com/wenhua/tcpservice/
│           │   ├── tcp/TCPServer.java    # TCP服务器
│           │   ├── mapper/EnvMapper.java # 数据访问层
│           │   └── pojo/dev/Device.java  # 设备实体类
│           └── pom.xml
│
└── 资料/                                  # 项目文档
    ├── 接口文档.md
    └── 命令文档.txt
```

---

## 🛠 技术栈

### 前端

- **Vue 3.5** + Element Plus
- Vite 构建工具
- Axios 网络请求
- Vue Router 路由管理
- Pinia 状态管理
- ECharts 数据可视化

### 后端

- **Spring Boot 2.x**
- TCP Socket 通信（自定义协议）
- MyBatis 数据持久化
- MySQL 数据库
- RESTful API 设计

### 嵌入式

- **HarmonyOS** 轻量系统
- **BearPi-HM Nano**（Hi3861芯片）
- **E53_IA1** 智慧农业扩展板（SHT30温湿度 + BH1750光照）
- **E53_SC1** 智慧路灯扩展板（BH1750光照）
- WiFi STA模式联网
- TCP Client通信
- cJSON 数据序列化

---

## ⚙️ 快速开始

### 1. 配置嵌入式设备

编辑全局配置文件：

```c
// flushbonading/this/global_settings.h

// WiFi配置
#define WIFI_NAME "你的WiFi名称"
#define WIFI_PASSWORD "你的WiFi密码"

// 服务器配置
#define SERVER_IP "192.168.1.100"    // 平台服务器IP
#define SERVER_PORT 8888              // TCP服务端口

// 设备配置
#define DEVICE_ID "ENV000100001"      // 设备唯一编号
```

### 2. 启动后端服务

```bash
cd platform/env-springboot/TCPService

# 编译
mvn clean package

# 运行
java -jar target/TCPService-1.0-SNAPSHOT.jar
```

服务启动后：

- TCP服务监听端口：`8888`
- REST API地址：`http://localhost:8080`

### 3. 启动前端

```bash
cd platform/env-monitor-ui

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

访问：`http://localhost:8080`

### 4. 编译烧录嵌入式程序

```bash
cd flushbonading/this

# 编译
python build.py BearPi-HM_Nano

# 使用HiBurn工具烧录到开发板
```

---

## 📡 通信协议

### 设备上报数据格式（JSON）

```json
{
  "returnIntent": "REPORT",
  "id": "ENV000100001",
  "name": "环境监测设备-ENV000100001",
  "ip": "192.168.1.105",
  "onlineStatus": 1,
  "temperature": 25.5,
  "humidity": 60,
  "lightIntensity": 10000,
  "auto": 1,
  "interval": 5000,
  "alertStatus": 0,
  "deviceType": "环境监测",
  "firmwareVersion": "1.0.0",
  "uptime": 3600,
  "signalStrength": -50,
  "batteryLevel": 85,
  "lampStatus": 1,
  "brightness": 80,
  "faultStatus": 0,
  "powerConsumption": 45.5
}
```

### 平台下发控制命令

| 命令              | 功能         | 示例                                        |
| ----------------- | ------------ | ------------------------------------------- |
| `REPORT`          | 查询设备状态 | `{"cmd":"REPORT"}`                          |
| `SET`             | 设置参数     | `{"cmd":"SET","interval":10000}`            |
| `AUTO_ON`         | 开启自动监测 | `{"cmd":"AUTO_ON"}`                         |
| `AUTO_OFF`        | 关闭自动监测 | `{"cmd":"AUTO_OFF"}`                        |
| `LAMP_ON`         | 开灯         | `{"cmd":"LAMP_ON"}`                         |
| `LAMP_OFF`        | 关灯         | `{"cmd":"LAMP_OFF"}`                        |
| `LAMP_BRIGHTNESS` | 调节亮度     | `{"cmd":"LAMP_BRIGHTNESS","brightness":50}` |
| `CONTROL`         | 通用控制     | `{"cmd":"CONTROL","action":"LAMP_ON"}`      |
| `REBOOT`          | 重启设备     | `{"cmd":"REBOOT"}`                          |
| `CALIBRATE`       | 校准传感器   | `{"cmd":"CALIBRATE"}`                       |

---

## 🔧 核心功能说明

### 环境监测

- 使用 **E53_IA1** 扩展板采集温湿度（SHT30传感器）和光照强度（BH1750传感器）
- 使用 **E53_SC1** 扩展板采集光照强度（BH1750传感器）
- 支持自动监测模式，按设定间隔自动上报数据
- 温度/湿度/光照异常自动告警

### 路灯控制

- 支持远程开关灯
- 亮度调节范围：0-100%
- 实时功耗计算（根据亮度模拟）
- 故障状态检测与上报

### 设备管理

- 设备自动注册与上线
- 心跳保活机制（30秒间隔）
- 设备状态实时监控
- 固件版本管理

---

## 📝 数据库表结构

```sql
CREATE TABLE Device (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    ip VARCHAR(50),
    port INT,
    deviceType VARCHAR(50),
    temperature FLOAT,
    humidity INT,
    lightIntensity INT,
    onLine INT,
    lampStatus INT,
    brightness INT,
    faultStatus INT,
    powerConsumption FLOAT,
    firmwareVersion VARCHAR(20),
    uptime BIGINT,
    signalStrength INT,
    batteryLevel INT,
    alertStatus INT,
    lastReportTime TIMESTAMP
);
```

---

## 🐛 常见问题

### 1. 设备无法连接WiFi

- 检查 `global_settings.h` 中的WiFi名称和密码是否正确
- 确认WiFi为2.4GHz频段（Hi3861不支持5GHz）

### 2. 设备无法连接平台

- 检查 `SERVER_IP` 是否为平台服务器实际IP
- 确认防火墙已开放TCP端口8888
- 确保设备与服务器在同一网络或网络互通

### 3. 传感器数据异常

- 检查E53扩展板是否正确安装
- 确认I2C接口连接正常
- 尝试重新校准传感器

---

## 🤝 贡献方式

1. Fork 本仓库
2. 新建分支 `feature/xxx` 或 `fix/xxx`
3. 提交代码并发起 Pull Request

---

## 📜 License

MIT License

---

## 👥 团队

**EnvSense 智能环境监测团队**
