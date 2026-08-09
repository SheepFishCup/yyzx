# 颐养中心微服务 (yyzx-microservices)

基于 Spring Cloud Alibaba 的微服务架构，由原单体 `yyzx_backend` 拆分而来。

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 2.7.18 |
| Spring Cloud | 2021.0.9 |
| Spring Cloud Alibaba | 2021.0.6.1 |
| Nacos | 2.2.3 (注册中心 + 配置中心) |
| Spring Cloud Gateway | (API 网关) |
| MyBatis-Plus | 3.5.1 |
| Druid | 1.2.6 |
| Redis + Redisson | 7.x / 3.17.7 |
| RabbitMQ | 3.12 |
| Quartz | 2.3.2 |
| Knife4j | 4.4.0 |
| Java | 1.8 |

## 模块架构

```
yyzx_microservices/
├── yyzx-common/         公共模块 (jar) — 实体、DTO、VO、工具类、常量
├── yyzx-gateway/        API网关 (:8080) — Spring Cloud Gateway + JWT认证
├── yyzx-auth/           认证服务 (:8081) — 登录、用户、角色、菜单
├── yyzx-customer/       客户服务 (:8082) — 客户CRUD、偏好、护理项目、小程序
├── yyzx-bed/            床位服务 (:8083) — 床位、房间、床位交换
├── yyzx-nursing/        护理服务 (:8084) — 护理项目、等级、记录
├── yyzx-checkinout/     出入管理 (:8085) — 退住、外出审批
├── yyzx-meal/           膳食服务 (:8086) — 菜品、膳食计划
├── yyzx-notification/   通知服务 (:8087) — RabbitMQ消费、邮件、钉钉、WebSocket
├── yyzx-report/         报表服务 (:8088) — 客户统计、财务Excel导出
├── yyzx-task/           定时任务 (:8089) — Quartz定时任务
├── nacos-config/        Nacos 配置中心文件
├── docker-compose.yml   Docker 容器编排
└── .env                 环境变量
```

## 快速启动

### 1. 本地开发

**前置条件：** MySQL 8.0、Redis 7、RabbitMQ 3.12、Nacos 2.x 已启动

```bash
# 编译 common 模块
cd yyzx_microservices
mvn clean install -pl yyzx-common -DskipTests

# 启动各服务（逐个或批量）
mvn spring-boot:run -pl yyzx-auth
mvn spring-boot:run -pl yyzx-customer
# ... 依次启动所有服务

# 访问
# 网关:     http://localhost:8080/yyzx/doc.html
# Nacos:    http://localhost:8848/nacos
# Druid监控: http://localhost:8081/yyzx/druid
```

### 2. Docker Compose 部署

```bash
cd yyzx_microservices

# 先编译 common（Docker 构建需要）
mvn clean install -pl yyzx-common -DskipTests

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f yyzx-gateway

# 停止
docker-compose down
```

### 3. 运行测试

```bash
# 全部模块测试（需本地 MySQL/Redis 可用）
mvn test

# 单个模块
mvn test -pl yyzx-auth
```

## Nacos 配置导入

1. 启动 Nacos
2. 访问 http://localhost:8848/nacos
3. 在"配置管理 → 配置列表"中逐个导入 `nacos-config/` 目录下的 YAML 文件
4. Data ID 为文件名，Group 为 `DEFAULT_GROUP`
5. 先导入 `yyzx-common.yaml`（共享配置），再导入各服务配置
6. 在服务的 `application.yaml` 中将 `spring.cloud.nacos.config.enabled` 改为 `true`

## 网关路由

| 路径前缀 | 目标服务 |
|---------|---------|
| `/admin/**`, `/role/**`, `/menu/**` | yyzx-auth |
| `/customer/**`, `/patient/**` | yyzx-customer |
| `/bed/**`, `/room/**`, `/beddetails/**` | yyzx-bed |
| `/nursecontent/**`, `/nurselevel/**` | yyzx-nursing |
| `/backdown/**`, `/outward/**` | yyzx-checkinout |
| `/food/**`, `/meal/**` | yyzx-meal |
| `/report/**` | yyzx-report |

## 与原单体项目的差异

1. **认证方式**：Gateway 统一 JWT 校验 → 下游服务通过 `X-User-Id` 头获取用户
2. **跨服务调用**：共享数据库直连（暂未引入 OpenFeign）
3. **RabbitMQ**：交换机/队列由 `yyzx-notification` 声明，其他服务只发送消息
4. **报表模块**：重写了原 `ReportServiceImpl` 的 9 个 bug
5. **定时任务**：`BackdownAutoApproveJob` 改为直接使用 Mapper（不再跨服务调用 Service）
6. **配置**：所有硬编码绝对路径已改为相对路径 / classpath
