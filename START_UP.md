# LampOptim — 项目初始化指南

## 前置要求

- JDK 21+
- Maven 3.8+ (或使用项目自带的 `mvnw`)
- MySQL 8.0+
- 一个兼容 OpenAI API 的 AI 服务商 API Key（如 DeepSeek）

---

## 1. 数据库初始化

项目默认连接 `localhost:3306/light` 数据库。

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `light` DEFAULT CHARACTER SET utf8mb4;

-- 导入表结构
source init_user.sql;
```

连接参数可通过环境变量覆盖：

| 环境变量          | 作用     | 默认值    |
|---------------|--------|--------|
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码  | **必填** |

---

## 2. AI API Key 配置

Spring AI 集成（`/ai/chat` 等端点）需要设置 API Key：

| 环境变量         | 作用                | 默认值                        |
|--------------|-------------------|----------------------------|
| `AI_API_KEY` | OpenAI 兼容 API 的密钥 | 可选（留空则降级为 MockedChatModel） |

如果未设置 `AI_API_KEY`，程序将自动降级为 `MockedChatModel`，返回预设的模拟回复（仅用于开发测试）。

**DeepTalker**（另一个独立 AI 调用模块）使用单独的密钥：

| 环境变量                  | 作用                  | 默认值                       |
|-----------------------|---------------------|---------------------------|
| `DEEP_TALKER_API_KEY` | DeepTalker 的 API 密钥 | `YOUR_API_KEY_HERE`（未设置时） |

> 可通过 `application.properties` 调整 AI 参数：
> - `spring.ai.openai.base-url` — API 地址（默认 `https://api.deepseek.com`）
> - `spring.ai.openai.chat.options.model` — 模型名称（默认 `deepseek-v4-flash`）
> - `spring.ai.openai.chat.options.temperature` — 温度参数（默认 `0.7`）

---

## 3. 启动方式

### 使用 Maven Wrapper（推荐）

```bash
cd platform/env-springboot/TCPService

# Linux / macOS
AI_API_KEY=sk-xxx DB_PASSWORD=your_db_pwd ./mvnw spring-boot:run

# Windows PowerShell
$env:AI_API_KEY="sk-xxx"; $env:DB_PASSWORD="your_db_pwd"; ./mvnw.cmd spring-boot:run
```

### 打包后运行

```bash
./mvnw clean package -DskipTests
java -jar target/TCPService-0.0.1-SNAPSHOT.jar
```

---

## 4. 验证运行

服务启动后：

- HTTP 服务端口：`18081`
- 登录接口：`POST /login`
- AI 聊天：`POST /ai/chat`
- 状态分析：`POST /ai/analyze`
- 趋势预测：`POST /ai/predict`

---

## 5. 开发说明

- 测试使用 `MockedChatModel` 模拟 AI 回复，无需真实 API Key
- 运行测试：`./mvnw test`
- JWT 签名密钥可在 `application.properties` 中配置