# 服务启动说明

> 颐养中心微服务本地开发与 Docker 部署启动指南

---

## 1. 前置条件

### 1.1 开发环境

| 软件 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 1.8 | `JAVA_HOME` 已配置 |
| Maven | 3.6.3+ | `mvn` 命令可用 |
| MySQL | 8.0 | 数据库 `yyzx` 已创建并初始化 |
| Redis | 7.x | 本地或远程可用 |
| RabbitMQ | 3.12 | 本地或远程可用 |
| Nacos | 2.2.x | 注册中心 + 配置中心（可选） |
| Sentinel Dashboard | 1.8.6 | 流量防卫兵控制台（可选） |
| SkyWalking OAP | 10.4.0 / 9.x | 链路追踪后端（可选） |
| SkyWalking Java Agent | 9.3.0 | `scripts/skywalking-agent/` 已就位 |
| Git | 任意 | 用于拉取代码 |

### 1.2 数据库初始化

```bash
# 导入原始项目的 SQL 文件（位于 yyzx_backend/yyzx.sql）
mysql -u root -p < ../yyzx_backend/yyzx.sql
```

### 1.3 中间件启动

```bash
# Docker 方式（推荐）
docker-compose up -d mysql redis rabbitmq

# Windows 手动启动
startup.cmd -m standalone                           # Nacos → :8848
java -jar sentinel-dashboard-1.8.6.jar --server.port=8090  # Sentinel → :8090
oapService.bat                                      # SkyWalking OAP → :11800
webappService.bat                                   # SkyWalking UI → :8085
```

### 1.4 SkyWalking Agent 验证

Agent jar 已在 `scripts/skywalking-agent/skywalking-agent.jar`（22MB）。  
启动脚本会自动检测并挂载，无需手动配置。如 Agent 不存在，服务仍可正常启动（仅无追踪）。

```bash
ls -lh scripts/skywalking-agent/skywalking-agent.jar
```

---

## 2. 启动顺序

> 严格按照以下顺序启动，前一个服务就绪后再启动下一个。

```
第1步: MySQL、Redis、RabbitMQ、Nacos（中间件）
第2步: yyzx-common（Maven 安装到本地仓库）
第3步: yyzx-notification（RabbitMQ 消费者，需提前声明队列）
第4步: yyzx-auth（认证服务，被网关依赖）
第5步: yyzx-customer（客户服务）
第6步: yyzx-bed（床位服务）
第7步: yyzx-nursing（护理服务）
第8步: yyzx-checkinout（出入管理）
第9步: yyzx-meal（膳食服务）
第10步: yyzx-report（报表服务）
第11步: yyzx-task（定时任务）
第12步: yyzx-gateway（API 网关 — 最后启动，作为统一入口）
```

---

## 3. 启动命令

### 3.1 第一步：编译 common 模块

```bash
cd yyzx_microservices
mvn clean install -pl yyzx-common -DskipTests
```

验证：
```
[INFO] BUILD SUCCESS
[INFO] yyzx-common ........................................ SUCCESS
```

### 3.2 第二步：启动通知服务（声明 RabbitMQ 队列）

```bash
mvn spring-boot:run -pl yyzx-notification
```

验证：
```
Started YyzxNotificationApplication in X seconds
# 访问 RabbitMQ 管理界面确认队列已创建：http://localhost:15672
```

### 3.3 第三步：启动认证服务

```bash
mvn spring-boot:run -pl yyzx-auth
```

验证：
```bash
# 验证码接口（无需 token）
curl http://localhost:8081/yyzx/admin/generate

# Knife4j 文档
# 浏览器访问: http://localhost:8081/yyzx/doc.html
```

### 3.4 第四步：启动客户服务

```bash
mvn spring-boot:run -pl yyzx-customer
```

验证：
```bash
curl http://localhost:8082/yyzx/doc.html
```

### 3.5 第五步：启动床位服务

```bash
mvn spring-boot:run -pl yyzx-bed
```

### 3.6 第六步：启动护理服务

```bash
mvn spring-boot:run -pl yyzx-nursing
```

### 3.7 第七步：启动出入管理

```bash
mvn spring-boot:run -pl yyzx-checkinout
```

### 3.8 第八步：启动膳食服务

```bash
mvn spring-boot:run -pl yyzx-meal
```

### 3.9 第九步：启动报表服务

```bash
mvn spring-boot:run -pl yyzx-report
```

### 3.10 第十步：启动定时任务

```bash
mvn spring-boot:run -pl yyzx-task
```

### 3.11 第十一步：启动 API 网关

```bash
mvn spring-boot:run -pl yyzx-gateway
```

验证：
```bash
# 通过网关访问（JWT 认证由网关统一处理）
curl http://localhost:8080/yyzx/admin/generate

# Knife4j 文档（走网关）
# 浏览器访问: http://localhost:8080/yyzx/doc.html
```

---

## 4. 一键批量启动（IDEA）

如果使用 IntelliJ IDEA：

1. 打开 `yyzx_microservices` 目录作为 Maven 项目
2. 为每个模块创建 Spring Boot 运行配置：
   - Main class: 各模块的 `*Application.java`
   - VM options: `-Dfile.encoding=UTF-8`
3. 创建 Compound Run Configuration，包含所有 10 个服务
4. 一键启动全部服务

---

## 5. SkyWalking + Sentinel 高可用模式

### 5.1 启动 Sentinel Dashboard

```bash
# 下载并启动 Sentinel Dashboard
java -jar sentinel-dashboard.jar --server.port=8080
# 访问: http://localhost:8080（默认账号 sentinel/sentinel）
```

### 5.2 使用 SkyWalking Agent 启动服务

```bash
# 1. 下载 SkyWalking Java Agent 并解压到 scripts/ 目录
# 2. 设置环境变量
export SKYWALKING_AGENT_HOME=/opt/skywalking-agent

# 3. 使用脚本启动（Linux/Mac）
cd scripts
chmod +x start-with-skywalking.sh
./start-with-skywalking.sh gateway   # 启动 gateway
./start-with-skywalking.sh all       # 启动 gateway+auth+customer

# 4. Windows
scripts\start-gateway.bat
scripts\start-auth.bat
scripts\start-customer.bat
```

### 5.3 验证 Sentinel 规则

```bash
# 查看 Sentinel 规则（需先配置 Dashboard）
curl http://localhost:8720/getRules?type=flow

# 手动触发限流（快速发送 >100 请求）
for i in $(seq 1 150); do
  curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8082/yyzx/customer/listKhxxPage?current=1&pageSize=5" &
done
# 预期部分返回 429 Too Many Requests
```

## 6. Docker Compose 一键部署

```bash
cd yyzx_microservices

# 先编译 common 到本地仓库
mvn clean install -pl yyzx-common -DskipTests

# 启动全部容器（构建 + 运行）
docker-compose up -d --build

# 查看启动日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f yyzx-gateway
docker-compose logs -f yyzx-auth

# 停止全部
docker-compose down
```

---

## 7. 验证服务启动成功

### 6.1 Nacos 服务列表

访问 `http://localhost:8848/nacos`，在"服务管理 → 服务列表"中应看到：

```
yyzx-gateway
yyzx-auth
yyzx-customer
yyzx-bed
yyzx-nursing
yyzx-checkinout
yyzx-meal
yyzx-notification
yyzx-report
yyzx-task
```

### 6.2 健康检查端点

每个服务访问其 Knife4j 文档页：
- Gateway:   `http://localhost:8080/yyzx/doc.html`
- Auth:      `http://localhost:8081/yyzx/doc.html`
- Customer:  `http://localhost:8082/yyzx/doc.html`
- Bed:       `http://localhost:8083/yyzx/doc.html`
- Nursing:   `http://localhost:8084/yyzx/doc.html`
- CheckInOut:`http://localhost:8085/yyzx/doc.html`
- Meal:      `http://localhost:8086/yyzx/doc.html`
- Notification: `http://localhost:8087/yyzx/doc.html`
- Report:    `http://localhost:8088/yyzx/doc.html`
- Task:      `http://localhost:8089/yyzx/doc.html`

### 6.3 功能验证

```bash
# 1. 获取验证码（无需认证）
curl http://localhost:8080/admin/generate

# 2. 登录获取 token
curl -X POST http://localhost:8080/admin/loginWithCaptcha \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456","captcha":"xxxx","uuid":"xxxx"}'

# 3. 使用 token 访问受保护接口
curl http://localhost:8080/customer/listKhxxPage?current=1\&pageSize=10 \
  -H "token: <your-jwt-token>"
```

### 6.4 常见启动问题

| 问题 | 原因 | 解决 |
|------|------|------|
| `Connection refused` | MySQL/Redis/RabbitMQ 未启动 | 确认中间件已启动 |
| `Unknown database 'yyzx'` | 数据库未初始化 | 执行 `yyzx.sql` |
| `Failed to bind to port` | 端口被占用 | 修改 `application.yaml` 端口或关闭占用进程 |
| `Cannot find yyzx-common` | common 模块未安装 | 先执行 `mvn install -pl yyzx-common` |
| `yyzx-auth is UNKNOWN` | Nacos 未连接 | 如不需要 Nacos，在配置中设为 `enabled: false` |
| `Failed to declare exchange` | RabbitMQ 队列冲突 | 删除 RabbitMQ 中已有队列重建 |
| Gateway 404 | 路由未匹配 | 检查 `application.yaml` 中的路由配置 |

---

## 8. 运行测试

```bash
# 全部模块测试
mvn test

# 单个模块测试（跳过需外部依赖的测试）
mvn test -pl yyzx-auth -Dspring.profiles.active=test

# 跳过测试编译
mvn clean package -DskipTests
```

> 注意：测试类默认禁用 Nacos 注册与 RabbitMQ 自动配置，但需要本地 MySQL 可用。如需完全离线测试，在 `application.yaml` 中额外排除数据源自动配置。

---

## 9. 快速验证脚本

全部服务启动后，复制粘贴以下命令验证：

```powershell
# 恢复 token 并测试全部核心接口
. .\scripts\token-init.ps1
@(
  '/admin/generate',
  '/admin/findUserPage?current=1',
  '/customer/listKhxxPage?current=1&pageSize=3',
  '/bed/findBed',
  '/room/listRoom',
  '/food/listFood',
  '/nurselevel/listNurseLevel',
  '/report/customerStats?startDate=2026-07-02&endDate=2026-08-01'
) | ForEach-Object {
  $r = call-api $_ 2>$null
  $status = if ($r.flag) { "OK" } else { "FAIL: $($r.message)" }
  Write-Host "$status  $_"
}
```

预期全部输出 `OK`。如果 token 过期先运行 `.\scripts\login.ps1`。
