# 智绿云控 全项目修复 Checklist

> 基于 [设计文档](资料/智绿云控%20-%20AI%20协同的环境监测与智能节能管控平台.docx) 与前后端实际代码的差距分析 + 代码本身的健壮性/安全问题汇总。
> 共 15 天任务, 按优先级排列。

---

## 第一天：P0 安全漏洞 — 密钥与硬编码凭据

### 1.1 DeepSeek API Key 硬编码在前端

- [x] 是否完成
- **文件**: `platform/frontend/src/api/auth.js:159`
- **问题**: `const DEEPSEEK_API_KEY = "sk-d3447a51c4c349c7bd269d1c9654cb25";` — API Key 明文硬编码在前端源码中
- **影响**: 任何人查看前端源码即可盗用 DeepSeek API 额度, 造成经济损失
- **修复**: 移除前端硬编码, 改为后端代理转发, API Key 存放在后端配置/环境变量中

### 1.2 高德地图 API Key 硬编码

- [x] 是否完成
- **文件**: `platform/frontend/src/components/map/LampMap.vue:63`
- **问题**: `const AMAP_KEY = "21d80a7334cb846ee23029681f541a06";` — 地图 Key 明文硬编码
- **影响**: 可被他人盗用, 导致配额耗尽或产生费用
- **修复**: 通过后端代理或环境变量注入

### 1.3 JWT 密钥硬编码

- [x] 是否完成
- **文件**: `platform/backend/TCPService/src/main/java/com/wenhua/tcpservice/util/JwtUtils.java:15`
- **问题**: `private static final String signKey = "itheima";` — 硬编码弱密钥
- **影响**: 可被暴力破解, 任意伪造令牌
- **修复**: 使用 jasypt 加密配置或环境变量注入

### 1.4 密码明文存储

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../UserServiceImpl.java`
- **问题**: 注释写明 `// 暂时省略了加密过程`, 密码明文入库
- **影响**: 数据库泄露即所有密码泄露
- **修复**: 集成 BCryptPasswordEncoder

### 1.5 登录明文传输密码

- [x] 是否完成
- **文件**: `platform/frontend/src/views/LoginView.vue`
- **问题**: 登录请求中密码明文传输, 无 HTTPS 时密码被中间人窃取
- **影响**: 网络监听可获取用户密码
- **修复**: 前端至少做一次哈希 + 后端强制 HTTPS

### 1.6 服务器 IP 硬编码

- [x] 是否完成
- **文件**: `platform/frontend/src/api/auth.js` 及多处 `.vue` 组件
- **问题**: `const api = "http://8.130.125.122:18081";` 及多处硬编码 `http://localhost:18081`
- **影响**: 环境切换需修改多处代码, 易出错
- **修复**: 统一使用环境变量 `VITE_API_BASE_URL`, 配置 Vite proxy

---

## 第二天：P0 权限绕过与注入

### 2.1 CMDTool — AI 可执行任意系统命令

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../ai/tools/CMDTool.java`
- **问题**: 从 AI 响应提取命令字符串通过 `Runtime.exec()` 执行
- **影响**: 攻击者通过 Prompt 注入可执行任意系统命令
- **修复**: 限制命令白名单, 禁止高危命令

### 2.2 FileTool — AI 可读写任意文件

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../ai/tools/FileTool.java`
- **问题**: AI 响应决定文件路径和内容, 无沙箱限制
- **影响**: 可读取 `/etc/passwd`、写入 webshell
- **修复**: 限制读写目录为专用沙箱路径

### 2.3 DeviceTypeController 缺失权限校验

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../controller/DeviceTypeController.java`
- **问题**: 所有写端点 (add/update/delete/toggleStatus) 均无身份验证
- **影响**: 任意用户（含匿名）可执行设备类型管理操作
- **修复**: 注入 UserMapper，添加 isAdmin() 方法并校验 ADMIN 权限

### 2.4 UserController/MaintenanceLogController 权限阈值错误 + 权限枚举化

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../controller/UserController.java`、`MaintenanceLogController.java`
- **问题**: 两处 `isRoot()` 均使用 `>= 2`，但管理员应要求等级 3
- **影响**: 运维工程师（等级 2）可执行管理员操作
- **修复**: 新建 `PermissionLevel` 枚举 + `PermissionLevelTypeHandler`，`User.userPermission` 改为枚举类型，比较统一使用 `hasPermission(PermissionLevel.ADMIN)`

### 2.5 MonitorRecordController.delete() 无权限校验

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../controller/MonitorRecordController.java`
- **问题**: 删除监控记录无任何认证/鉴权
- **影响**: 任意匿名用户可删除数据
- **修复**: 添加权限拦截

### 2.6 `/login` 唯一放行路径

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../config/WebConfig.java`
- **问题**: 拦截器只排除了 `/login`, 静态资源、错误路径、其他公开接口均被拦截
- **影响**: 前端页面资源无法加载, 或误拦截正常请求
- **修复**: 添加排除路径列表

---

## 第三天：认证与路由安全

### 3.1 前端无路由守卫

- [x] 是否完成
- **文件**: `platform/frontend/src/router/index.js`
- **问题**: 仅 3 条路由定义, 无 `router.beforeEach` 导航守卫, 未登录用户可直接访问所有页面
- **影响**: 任意用户可绕过登录直接查看仪表盘等页面
- **修复**: 添加全局前置守卫, 检查 token 是否存在, 未登录重定向到 `/login`

### 3.2 Token 无刷新机制

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../util/JwtUtils.java:18`
- **问题**: 过期时间 12h, 无 refresh_token, 过期后用户必须重新登录
- **影响**: 用户体验差, token 泄露后长期有效
- **修复**: 添加双 Token 机制(access + refresh)

### 3.3 前端无 Axios 请求拦截

- [x] 是否完成
- **文件**: `platform/frontend/src/api/auth.js`
- **问题**: Axios 实例有 response 拦截器但缺少统一的 401 自动登出逻辑
- **影响**: Token 过期后请求静默失败, 用户无感知
- **修复**: 在 response 拦截器中添加 401 处理, 自动跳转登录页

### 3.4 CloudPlatform 云凭据存储在 localStorage

- [x] 是否完成
- **文件**: `platform/frontend/src/components/cloud/CloudPlatform.vue:860`
- **问题**: 阿里云/华为云等 IoT 平台 AccessKey/Secret 存储在 localStorage
- **影响**: XSS 攻击可窃取云平台凭据, 控制真实云资源
- **修复**: 凭据仅在后端存储, 前端只展示掩码信息

---

## 第四天：数据库与数据完整性

### 后注

Day4的多数问题大概率已经失效. 在Day3, 该项目的数据库初始化脚本就已经被重构和优化, 仅存在`init_schema.sql`, `init_samples.sql`.
这两个问题都默认初始化或创建`light`架构. 在核对剩下的问题是否存在时, 请查看这些文件.

### 4.1 数据库名称不一致 (已在Day3重构中解决, init_schema.sql/application.properties 均已统一使用 `light`)

- [x] 是否完成
- **文件**: `init.sql` vs `application.properties`
- **问题**: `init.sql` 使用数据库 `envsense`, `application.properties` 连接 `jdbc:mysql://localhost:3306/light`
- **影响**: 部署时直接失败
- **修复**: 统一为 `light`, 修正 `init.sql` 首行

### 4.2 init.sql 与 Mapper XML 字段不匹配 (已在Day3重构中解决, monitor_record 字段已统一为 data_type/value/create_time)

- [x] 是否完成
- **问题**: `init.sql` 的 `monitor_record` 表使用旧字段(`temperature`/`humidity`/`lightIntensity`/`record_time`), 但 Mapper XML 和代码中使用新字段(`data_type`/`value`/`create_time`)
- **影响**: 查询/插入数据全部报错
- **修复**: 同步 schema 或提供迁移脚本

### 4.3 批量操作 N+1 查询

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../service/EnvServiceImpl.java`
- **问题**: 批量新增/删除/更新使用 `for` 循环调用单条 SQL
- **影响**: 100 条数据 = 101 次数据库交互, 性能极差
- **修复**: EnvMapper 新增 `batchDeleteDevices`/`batchUpdateDeviceStatus` 方法(`<foreach>`), EnvServiceImpl 改用批量调用

### 4.4 缺少事务管理

- [x] 是否完成
- **问题**: 多处需要事务的操作(点位批量创建、级联删除)未标注 `@Transactional`
- **影响**: 中间失败导致数据不一致
- **修复**: 在 EnvServiceImpl 批量操作方法上添加 `@Transactional(rollbackFor = Exception.class)`

### 4.5 EnvMapper.deleteDevice 使用错误注解

- [x] 是否完成
- **文件**: `platform/backend/TCPService/.../mapper/EnvMapper.java`
- **问题**: `@Update` — 应该使用 `@Delete`
- **影响**: 语义错误, MyBatis 可能拒绝执行
- **修复**: 改为 `@Delete`

---

## 第五天：TCP 服务器健壮性

### 5.1 无心跳超时机制

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../tcp/TCPServer.java`
- **问题**: `client.setSoTimeout(0)` 表示无限等待, 断线只靠 IO 异常检测；无定期心跳检测
- **影响**: 已断开但未触发的 TCP 半开连接永久占用资源, 最终 OOM
- **修复**: 添加心跳超时检测 + 闲置清理线程

### 5.2 原始线程管理

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../tcp/TCPServer.java:52`
- **问题**: `new Thread(clientHandler).start()` — 无线程池, 无线程复用
- **影响**: 1000+ 设备并发时创建大量线程, 系统资源耗尽
- **修复**: 改用线程池(ThreadPoolExecutor 或 Spring @Async)

### 5.3 JSON 解析无异常保护

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../tcp/TCPServer.java`
- **问题**: `JSONObject.parseObject()` 可能抛出异常, 未被 try-catch 包裹
- **影响**: 畸形数据导致客户端处理线程崩溃
- **修复**: 添加 try-catch 和错误响应

---

## 第六天：后端业务逻辑 Bug

### 6.1 峰谷电计算使用当前时间

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../service/EnergyBillService.java`
- **问题**: `LocalDateTime.now().getHour()` — 计算整月账单也使用调用时刻的 hour
- **影响**: 每天 24 小时全用同一电价计算, 数据完全错误
- **修复**: 应遍历每天每小时的用电数据分别计算

### 6.2 账单数据未持久化

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../service/EnergyBillService.java`
- **问题**: `generateMonthlyBill()` 生成 `EnergyBill` 对象返回, 但不调用 `CampusEnergyMapper.insertBill()`
- **影响**: 每次获取账单都重新计算, 历史账单无法查询
- **修复**: 添加持久化逻辑

### 6.3 硬编码电价

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../service/EnergyBillService.java`
- **问题**: 峰/谷电价硬编码在代码中
- **修复**: 提取到配置表或 `application.properties`

### 6.4 照明优化结果不持久化

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../ai/SmartLightingEngine.java`
- **问题**: 调优方案仅返回 JSON string, 不写入 DB
- **影响**: 每次请求重新计算, 无法追踪优化效果
- **修复**: 设计 `optimization_record` 表保存结果

### 6.5 能耗统计返回硬编码趋势

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../ai/SmartLightingEngine.java`
- **问题**: 返回的月度趋势数据是硬编码的假数据
- **影响**: 前端图表展示不真实数据
- **修复**: 从真实用电记录聚合计算

### 6.6 路灯功率计算不一致

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../ai/SmartLightingEngine.java`
- **问题**: 一处使用 150W, 另一处使用 80W 作为单灯功率
- **影响**: 节省能耗计算偏差
- **修复**: 统一从设备表读取真实功率

### 6.7 教学案例不同步到 DB

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../service/TeachingCaseService.java`
- **问题**: `createTeachingCase()` 未调用 Mapper
- **影响**: 教学案例创建后丢失
- **修复**: 添加 `CampusEnergyMapper.insertCase()`

### 6.8 教学统计返回虚假数字

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../service/TeachingCaseService.java`
- **问题**: `devices.size() * 3` 作为统计数据
- **影响**: 统计数字无意义
- **修复**: 从实际使用记录计算

---

## 第七天：SQL 与 ORM 隐患

### 7.1 数值字段 LIKE 查询

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../EnvMapper.xml`
- **问题**: `temperature LIKE '%${...}%'` 等 — 数字字段全文匹配 + `$` 拼接存在 SQL 注入风险
- **影响**: 全表扫描 + SQL 注入
- **修复**: 改为范围查询或精准匹配, 使用 `#{}` 预编译

### 7.2 密码字段 LIKE 查询

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../UserMapper.xml`
- **问题**: `password LIKE '%${keyword}%'`
- **影响**: SQL 注入 + 可模糊匹配密文字段
- **修复**: 密码不应参与模糊查询

### 7.3 `Result` POJO 未使用 @Data

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../pojo/Result.java`
- **问题**: 手动编写 getter/setter
- **影响**: 维护成本高
- **修复**: 改用 Lombok `@Data`

### 7.4 缺省连接池配置

- [ ] 是否完成
- **问题**: 依赖中包含 HikariCP, 但 `application.properties` 无连接池参数
- **影响**: 高并发下连接耗尽
- **修复**: 添加 `spring.datasource.hikari.*` 配置

---

## 第八天：前端假数据与仿真模块 (P1)

### 8.1 所有 ECharts 图表使用随机数据

- [ ] 是否完成
- **涉及组件**:
  - `EnvironmentChart.vue` — 完全使用 `Math.random()` 生成数据
  - `CampusEnergy.vue` — 8 个 ECharts 实例全部硬编码/随机数据
  - `EcologyEngine.vue` — 5 个 ECharts 实例全部随机数据
  - `AIPrediction.vue` — 趋势图表使用随机数据
  - `CloudPlatform.vue` — 实时数据展示使用随机数据
- **影响**: 所有可视化图表展示的是虚假数据, 完全无实际参考价值
- **修复**: 对接真实后端 API, 移除所有 `Math.random()` 数据生成

### 8.2 前端组件大面积使用 Mock 数据

- [ ] 是否完成
- **涉及组件**:
  - `DeviceManager.vue` — API 失败时降级到 14 个硬编码模拟设备
  - `MonitorRecord.vue` — 初始统计硬编码 (1256/1180/56/20), API 失败降级到模拟记录
  - `EcologyEngine.vue` — 6 个绿化区域数据全部硬编码, 无 API 调用
  - `TeachingLinkage.vue` — 全部数据硬编码(4 场景/4 资源/4 公告/3 课程案例)
  - `CloudPlatform.vue` — 所有连接/同步操作本地 `setTimeout` 模拟
  - `LampManager.vue` — 控制策略硬编码, 数据来自模拟接口
- **影响**: 前端展示的绝大部分数据不是真实数据, 系统不可用
- **修复**: 逐步替换为真实 API 调用, 仅在开发环境保留 mock

### 8.3 AI 员工监控完全模拟

- [ ] 是否完成
- **文件**: `AIPrediction.vue` 第三区域
- **问题**: 使用 `setInterval` + `Math.random()` 模拟实时监控数据
- **影响**: 无实际监控功能, 属于"假功能"
- **修复**: 对接 WebSocket 或轮询真实监控接口

### 8.4 AI 聊天绕过后端直接调用 DeepSeek

- [ ] 是否完成
- **文件**: `platform/frontend/src/api/auth.js:148-170`
- **问题**: AI 聊天前端直接用 `fetch()` 调用 DeepSeek API, 完全绕过 Spring Boot 后端
- **影响**: API Key 泄露(已修复 1.1), 无法在服务端做审计/限流/日志
- **修复**: 后端封装 AI 代理接口, 前端请求统一走后端

### 8.5 AI 预测本地生成分析结果

- [ ] 是否完成
- **文件**: `AIPrediction.vue`
- **问题**: `generateLocalAnalysis()` 和 `generateLocalChatResponse()` 返回硬编码文本模板 + 随机填入统计数字
- **影响**: AI 分析结果不是真实的 AI 推理, 属于"假 AI"
- **修复**: 统一走后端 AI 服务, 移除前端本地生成逻辑

---

## 第九天：文档与实现差距

### 9.1 AI 算法落地缺失

- [ ] 是否完成
- **文档声称**: 环境多因子关联分析、异常识别与趋势预测、自适应节能算法
- **实际**: 仅有 `DeepTalker` 调用大模型对话, 无任何传统 ML 算法实现
- **修复**: 实现基础统计分析(移动平均、阈值异常检测)

### 9.2 传感器"毫秒级高频采集"未实现

- [ ] 是否完成
- **文档声称**: 毫秒级高频采集、本地缓存、断点续传
- **实际**: TCP 数据上报间隔由设备端决定, 后端无缓存/续传机制; 前端无实时数据展示
- **修复**: 建立 WebSocket 推送机制, 实现前端实时数据更新

### 9.3 "告警响应延迟 < 500ms"无验证

- [ ] 是否完成
- **文档声称**: 告警响应延迟 < 500ms
- **实际**: 无告警模块性能测试或埋点; 前端无告警实时推送 UI
- **修复**: 建立告警模块 + 性能埋点

### 9.4 角色权限与实际实现不符

- [ ] 是否完成
- **文档**: 观察员(1) / 运维工程师(2) / 系统管理员(3) 三级权限
- **实际**:
  - `UserManagement.vue` 仅支持 普通用户(1) / 管理员(2)
  - `HomeView.vue` 的 `roleText` 仅返回 "管理员"/"操作员" 两种
  - 后端 `UserController.isRoot()` 阈值 `permission >= 2`
- **修复**: 统一为三级权限模型, 前后端对齐

### 9.5 缺少个人中心

- [ ] 是否完成
- **文档声称**: 密码修改、个人信息维护、操作记录、待办告警查看
- **实际**: 前端完全无个人中心页面/弹窗; 后端可能无对应接口
- **修复**: 实现个人中心各功能模块

### 9.6 缺少告警管理模块

- [ ] 是否完成
- **文档声称**: 异常记录、处理状态、闭环追踪全流程管理
- **实际**: 前端无告警管理完整页面; `MonitorRecord.vue` 只有简单的监控记录列表
- **修复**: 实现告警管理的完整 UI 和后端接口

### 9.7 缺少实时监测大屏

- [ ] 是否完成
- **文档声称**: 环境数据、设备状态、告警信息一屏总览
- **实际**: `HomeView.vue` 首页仅包含侧边菜单和组件切换, 无真正的仪表盘总览
- **修复**: 设计并实现实时监测大屏页面

---

## 第十天：前端健壮性问题

### 10.1 localStorage Key 不匹配 Bug

- [ ] 是否完成
- **问题**: `HomeView.vue` 读取 `localStorage.getItem("name")` 但 `LoginView.vue` 保存的是 `localStorage.getItem("username")`
- **影响**: 用户名始终显示为空白/null
- **修复**: 统一 key 名称

### 10.2 UserManagement 删除接口字段不一致

- [ ] 是否完成
- **文件**: `platform/frontend/src/components/user/UserManagement.vue`
- **问题**: API 调用 `deleteUserApi(user.userId)` 但删除按钮禁用条件为 `scope.row.userId === 1` — 实际后端可能接收 `id` 而非 `userId`
- **影响**: 删除操作可能发送错误字段名导致 400/404
- **修复**: 统一字段名为 `id` 或 `userId`, 与后端对齐

### 10.3 AIPrediction 混用 Axios 和 Raw Fetch

- [ ] 是否完成
- **文件**: `AIPrediction.vue`
- **问题**: 部分请求使用 `axios` 实例(带 token), 部分使用原生 `fetch("http://localhost:18081/...")`(不带 token 且硬编码 URL)
- **影响**: 某些请求无认证头被后端拦截, 硬编码 URL 导致部署环境切换失败
- **修复**: 统一使用 Axios 实例, 移除所有原生 fetch 调用

### 10.4 无全局错误边界

- [ ] 是否完成
- **问题**: 前端无任何 ErrorBoundary 或 `app.config.errorHandler` 配置
- **影响**: 组件渲染异常导致白屏, 无降级 UI
- **修复**: 添加全局错误处理 + 组件级错误边界

### 10.5 CampusEnergy.vue 冗余代码

- [ ] 是否完成
- **文件**: `platform/frontend/src/components/campus/CampusEnergy.vue` (2990 行)
- **问题**: 超大组件, 存在重复的费用统计区域(~100 行冗余), 职责不单一
- **影响**: 维护困难, 容易引入 Bug
- **修复**: 拆分为多个子组件, 删除冗余代码

---

## 第十一天：前端性能问题

### 11.1 setInterval 未正确清理

- [ ] 是否完成
- **涉及组件**: `AIPrediction.vue` (AI 员工监控区域)
- **问题**: `setInterval` 模拟数据更新, 组件卸载时未调用 `clearInterval`
- **影响**: 组件卸载后定时器持续运行, 造成内存泄漏
- **修复**: 在 `onUnmounted` 中清理所有定时器

### 11.2 ECharts 实例未正确销毁

- [ ] 是否完成
- **涉及组件**: `AIPrediction.vue`, `EcologyEngine.vue` 等
- **问题**: ECharts 实例在组件卸载时未调用 `dispose()`
- **影响**: 内存泄漏, 页面长时间运行后卡顿
- **修复**: 在 `onUnmounted` 中执行 `chart.dispose()`

### 11.3 无组件懒加载

- [ ] 是否完成
- **问题**: `HomeView.vue` 通过 `v-if` + `component` 动态切换所有子组件, 首次加载全部组件
- **影响**: 首屏加载时间过长, 初始包体积大
- **修复**: 使用 `defineAsyncComponent` 实现组件懒加载

### 11.4 图表窗口 resize 未处理

- [ ] 是否完成
- **问题**: 部分 ECharts 实例未监听 `window.resize` 事件
- **影响**: 浏览器窗口缩放后图表显示异常
- **修复**: 添加 `window.addEventListener('resize', ...)` 和 `chart.resize()`

---

## 第十二天：前端 UI/UX 问题

### 12.1 加载状态缺失

- [ ] 是否完成
- **问题**: 大多数组件在数据请求期间无 loading 状态或 skeleton 占位
- **影响**: 用户无法感知数据加载进度, 体验差
- **修复**: 为所有数据请求添加 `v-loading` 或骨架屏

### 12.2 空状态处理缺失

- [ ] 是否完成
- **问题**: 列表/表格在无数据时大多数未显示空状态提示
- **影响**: 空白页面让用户困惑
- **修复**: 添加 `<el-empty>` 空状态组件

### 12.3 错误提示不友好

- [ ] 是否完成
- **问题**: API 请求失败时大部分组件仅 `console.error` 或使用 ElMessage, 缺乏统一的错误处理策略
- **影响**: 用户无法理解错误原因
- **修复**: 统一错误处理 + 用户友好的错误提示

### 12.4 费用统计页面为空白占位

- [ ] 是否完成
- **文件**: `CampusEnergy.vue` 费用统计区域
- **问题**: 标题下方仅显示 "费用统计功能开发中"
- **影响**: 功能缺失, 不符合文档承诺
- **修复**: 实现费用统计图表

### 12.5 分页器无实际功能

- [ ] 是否完成
- **文件**: `MonitorRecord.vue`
- **问题**: 分页组件存在但 `@current-change` 事件未正确触发 API 重新请求
- **影响**: 翻页无实际效果
- **修复**: 绑定 `@current-change` 事件处理函数, 重新请求数据

### 12.6 缺少 CORS 配置

- [ ] 是否完成
- **问题**: 后端无 `@CrossOrigin` 或全局 CORS 配置; 前端 Vite 无 proxy
- **影响**: 开发环境跨域请求被浏览器拦截
- **修复**: 后端添加 `WebMvcConfigurer` CORS 配置 + 前端配置 Vite proxy

---

## 第十三天：日志、异常与监控

### 13.1 拦截器 INFO 日志泛滥

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../config/LoginCheckInterceptor.java`
- **问题**: `preHandle`、`postHandle`、`afterCompletion` 三个方法均打印 INFO 日志
- **影响**: 生产环境每请求输出 4+ 行日志, 磁盘 IO 和存储成本急剧上升
- **修复**: 改为 DEBUG 级别

### 13.2 缺少全局异常处理器

- [ ] 是否完成
- **问题**: 无 `@RestControllerAdvice`, 未处理参数校验异常、类型转换异常、空指针等
- **影响**: 异常直接暴露给客户端, 可能泄露堆栈信息
- **修复**: 添加 `GlobalExceptionHandler`

### 13.3 缺少请求/响应日志 AOP

- [ ] 是否完成
- **问题**: 无请求日志切面, 生产排查问题困难
- **影响**: 无法追溯操作来源和请求参数
- **修复**: 添加 `@Around` 切面记录关键接口的入参和耗时

### 13.4 前端缺少埋点与性能监控

- [ ] 是否完成
- **问题**: 前端无页面访问埋点、API 请求耗时采集、错误上报
- **影响**: 无法监控用户行为和前端性能
- **修复**: 集成前端监控 SDK(如 Sentry 或自建)

---

## 第十四天：配置与枚举规范化

### 14.1 全局配置和常量分散

- [ ] 是否完成
- **问题**: 温度/湿度/光照阈值分散在 `GlobalConfiguration.java` 及各 Controller/Service 中
- **影响**: 修改阈值需要全文搜索替换
- **修复**: 统一收拢到配置类或 `application.properties`

### 14.2 设备类型使用魔法字符串

- [ ] 是否完成
- **问题**: `"lamp"` vs `"device"` 字符串散落在 `EnvServiceImpl` 的逻辑分支中
- **影响**: 拼写错误导致设备类型判断失败
- **修复**: 定义 `DeviceType` 枚举

### 14.3 无统一响应码定义

- [ ] 是否完成
- **问题**: `Result` 中 code=0/1 的含义未定义枚举
- **影响**: 前后端对接容易混淆
- **修复**: 定义 `ResponseCode` 枚举

### 14.4 前端 Vite 配置过于简单

- [ ] 是否完成
- **文件**: `platform/frontend/vite.config.js`
- **问题**: 无 proxy、无 `define` 全局变量、无 `cross-env`、无构建优化
- **影响**: 开发/生产环境切换困难, 跨域问题解决不彻底
- **修复**: 配置 Vite proxy + 环境变量 + 构建优化

---

## 第十五天：测试、部署与运维

### 15.1 缺少 Service 层单元测试

- [ ] 是否完成
- **问题**: 仅存在 Controller 集成测试和 `MockedChatModel` 测试
- **影响**: 业务逻辑变更无回归保障
- **修复**: 添加 `EnvServiceImplTest`、`EnergyBillServiceTest` 等

### 15.2 数据库集成测试缺失

- [ ] 是否完成
- **问题**: 无 `@DataJpaTest` 或 `@MybatisTest`；`@SpringBootTest` 会尝试连接真实 DB
- **影响**: CI 环境无数据库则测试全部失败
- **修复**: 集成 Testcontainers 或 H2 内存数据库

### 15.3 安全测试缺失

- [ ] 是否完成
- **问题**: 无 JWT 鉴权功能测试、无注入攻击测试
- **修复**: 添加 `@WebMvcTest` + `SecurityMockMvcRequestPostProcessors`

### 15.4 缺少 Docker 部署配置

- [ ] 是否完成
- **问题**: 无 `Dockerfile` 和 `docker-compose.yml`
- **影响**: 部署依赖手动配置

### 15.5 缺少健康检查端点

- [ ] 是否完成
- **问题**: 未集成 `spring-boot-starter-actuator`
- **影响**: 无法监控应用健康状况

### 15.6 缺少 API 文档生成

- [ ] 是否完成
- **问题**: 未集成 SpringDoc OpenAPI / Swagger
- **影响**: 前端对接需要手动查看代码

### 15.7 数据库版本迁移缺失

- [ ] 是否完成
- **问题**: 无 Flyway / Liquibase 配置, schema 变更靠手动执行 SQL
- **影响**: 多环境数据库版本不一致

### 15.8 AI 工具 XML 解析无 schema 校验

- [ ] 是否完成
- **文件**: `platform/backend/TCPService/.../ai/Worker.java` / `WorkerTool.java`
- **问题**: 直接解析 AI 输出的 XML 标签, 无格式校验
- **影响**: 格式错误导致服务异常或误操作
- **修复**: 添加 XML schema 校验 + 异常回退

---

## 附录：紧急修复优先级

| 优先级    | 任务                            | 影响         |
|--------|-------------------------------|------------|
| **P0** | 1.1 DeepSeek API Key 硬编码      | 经济盗用       |
| **P0** | 1.2 高德地图 Key 硬编码              | 配额盗用       |
| **P0** | 1.3 JWT 密钥硬编码                 | 任意伪造令牌     |
| **P0** | 1.4 密码明文存储                    | 数据库泄露即密码泄露 |
| **P0** | 2.1 CMDTool 命令注入              | 服务器沦陷      |
| **P0** | 2.2 FileTool 文件任意读写           | 数据泄露       |
| **P0** | 2.3 DeviceTypeController 权限绕过 | 越权操作       |
| **P0** | 3.1 前端无路由守卫                   | 未授权访问      |
| **P0** | 4.1 数据库名称不一致                  | 服务无法启动     |
| **P1** | 3.4 云凭据 localStorage 存储       | 云资源被盗      |
| **P1** | 6.1 峰谷电价计算 Bug                | 财务数据错误     |
| **P1** | 5.1 无心跳检测 + 5.2 原始线程          | 生产 OOM     |
| **P1** | 4.3 N+1 批量查询                  | 性能瓶颈       |
| **P1** | 8.1-8.5 前端假数据                 | 系统不可用      |
| **P2** | 其余所有任务                        | 质量与可维护性    |

---

> **总计**: ~85 项任务, 涵盖 P0 安全漏洞 9 项、P1 关键缺陷 10+ 项、P2 质量改进 60+ 项。
> 建议按优先级从 P0 → P1 → P2 的顺序推进, 核心安全修复后可保障系统基本可用。