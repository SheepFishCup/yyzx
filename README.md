# 颐养中心管理系统（yyzx）

> 东软颐养中心 —— 面向养老机构的综合服务管理平台

## 📖 项目简介

东软颐养中心是为老年人提供集体居住及配套服务的综合管理系统，涵盖入住管理、床位管理、护理管理、健康管家等功能模块。

项目提供两种架构：**Spring Boot 单体**（`yyzx_backend`）和 **Spring Cloud 微服务**（`yyzx_microservices`），前端统一使用 Vue3。

---

## 🏗 项目结构

```
yyzx/
├── yyzx_backend/           # 单体 Spring Boot 后端
├── yyzx_microservices/     # 微服务后端（11 模块）
├── yyzx_front/             # Vue3 前端
├── yyzx.sql                # 数据库初始化脚本
└── README.md
```

---

# 一、单体架构（yyzx_backend）

## 🛠 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.6 | 后端框架 |
| MySQL | 8.0 | 数据库 |
| MyBatis-Plus | 3.5.1 | ORM |
| Druid | 1.2.6 | 连接池 |
| Redis | 7.x | 缓存 + 分布式锁 |
| Redisson | 3.17.7 | 分布式锁 |
| RabbitMQ | 3.12 | 异步消息 |
| Quartz | 2.3.2 | 定时任务 |
| JWT | Auth0 3.10 + JJWT 0.9 | 认证 |
| WebSocket | — | 实时推送 |
| Knife4j | 4.4.0 | API 文档 |

## 📦 核心功能

| 模块 | 说明 |
|------|------|
| 客户管理 | 入住登记、退住登记、外出登记、膳食日历 |
| 床位管理 | 床位示意图、床位调换、使用详情 |
| 护理模块 | 护理项目、护理级别、护理记录（Redis Lua 库存扣减） |
| 健康管家 | 服务对象设置、日常护理录入 |
| 用户管理 | 系统用户 + RBAC 角色菜单 |

## 🚀 快速开始

```bash
cd yyzx_backend
# 确保 MySQL/Redis/RabbitMQ 已启动
mvn spring-boot:run
# 访问 http://localhost:9999/yyzx/doc.html
```

---

# 二、微服务架构（yyzx_microservices）

## 🛠 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 后端框架 |
| Spring Cloud | 2021.0.9 | 微服务套件 |
| Spring Cloud Alibaba | 2021.0.6.1 | Nacos + Sentinel |
| Spring Cloud Gateway | — | API 网关 |
| Nacos | 2.2.3 | 注册中心 + 配置中心 |
| OpenFeign | 3.1.9 | 服务间同步调用 |
| Resilience4j | 2.1.7 | Feign 熔断降级 |
| Sentinel | 1.8.6 | 流量防卫兵（QPS 限流） |
| SkyWalking | Agent 9.3.0 | 链路追踪 |
| MyBatis-Plus | 3.5.1 | ORM |
| Druid | 1.2.6 | 连接池 |
| Redis + Redisson | 7.x / 3.17.7 | 缓存 + 分布式锁 |
| RabbitMQ | 3.12 | 异步消息队列 |
| Quartz | 2.3.2 | 定时任务 |
| JWT | JJWT 0.9.1 | 无状态认证 |
| Knife4j | 4.4.0 | API 文档 |

## 📦 模块列表

| 模块 | 端口 | 说明 |
|------|------|------|
| `yyzx-common` | — | 公共模块（实体/DTO/VO/工具/Feign 接口/拦截器） |
| `yyzx-gateway` | 8080 | API 网关（无状态 JWT 认证 + Sentinel 限流 + SkyWalking 追踪） |
| `yyzx-auth` | 8081 | 认证服务（登录/验证码/用户/RBAC） |
| `yyzx-customer` | 8082 | 客户管理（Feign → Bed/Auth，Sentinel 限流保护） |
| `yyzx-bed` | 8083 | 床位管理（床位 CRUD + 房间 + 交换） |
| `yyzx-nursing` | 8084 | 护理服务（Redis Lua 原子库存扣减） |
| `yyzx-checkinout` | 8085 | 出入管理（Feign → Bed/Customer + Resilience4j 熔断） |
| `yyzx-meal` | 8086 | 膳食服务（菜品管理 + 图片上传） |
| `yyzx-notification` | 8087 | 通知服务（RabbitMQ 消费者 + 邮件 + 钉钉 + WebSocket） |
| `yyzx-report` | 8088 | 报表服务（重写版，修复原版 9 个 bug） |
| `yyzx-task` | 8089 | 定时任务（Quartz + Feign 调用） |

## 🏗 架构特点

- **Gateway 无状态 JWT 认证**：纯签名校验 + `X-User-Id` Header 传递，不依赖 Redis
- **OpenFeign 全覆盖**：5 个 Feign 接口 / 16 个端点取代跨模块 Mapper 直连
- **Resilience4j 降级**：超时 5s / 50% 失败率熔断 / 10s 半开，FallbackFactory 安全降级
- **Sentinel 流控**：Gateway 层限流 + `@SentinelResource`（QPS=100 + 异常比例熔断）
- **SkyWalking 追踪**：`[%X{traceId}]` 全链路日志传递
- **Internal 端点安全**：共享密钥 `X-Internal-Token` + `InternalAuthInterceptor` 校验
- **Mapper 治理**：跨模块冗余 Mapper 已清理 34 个文件，7/9 模块干干净净

## 🚀 快速开始

### 1. 首次启动

```bash
cd yyzx_microservices

# 安装父 POM + common
mvn clean install -N -DskipTests
mvn clean install -pl yyzx-common -DskipTests

# 一键启动全部 10 个服务（自动开独立窗口）
.\scripts\start-all.bat
```

### 2. 登录测试

```powershell
.\scripts\login.ps1                                    # 弹验证码 → 输入 → 自动保存 token

. .\scripts\token-init.ps1                             # 新终端恢复 token
call-api '/customer/listKhxxPage?current=1&pageSize=5'  # 测客户列表
call-api '/food/listFood'                                # 测菜品
call-api '/bed/findBed'                                  # 测床位
```

### 3. Docker 部署

```bash
docker-compose up -d
```

## 📚 文档索引

| 文档 | 说明 |
|------|------|
| `ARCHITECTURE.md` | 架构总览（Mermaid 图 + 技术栈 + 认证流程） |
| `ARCH_UPGRADE.md` | 架构升级记录（Feign/Sentinel/SkyWalking/InternalAuth） |
| `STRUCTURE.md` | 完整目录树 + 文件统计 |
| `STARTUP.md` | 启动说明（顺序/命令/Docker/验证） |
| `ROUTES.md` | API 路由一览（20 条 Gateway 路由 + 白名单） |
| `CODING.md` | 开发规范（包结构/命名/代码风格/Git 规范） |
| `FEIGN_CLIENTS.md` | Feign 调用清单（接口设计 + 迁移步骤） |
| `TEST_API.md` | 接口测试 curl 命令 + 预期返回 |
| `ERRORS.md` | 常见错误与解决方案（18+ 条实战记录） |
| `QUICK_REF.md` | 一页速查（端口/命令/中间件/启动速查） |
| `TODO.md` | 待办清单 + 已完成项追踪 |

## ✨ 项目亮点

- **JWT 无状态认证** + 5 次失败锁定 + 邮件密码重置
- **Redis Lua 原子库存扣减** + 乐观锁降级
- **布隆过滤器 + Redis Set** 登录黑名单
- **分布式锁 + 随机过期时间** 防缓存击穿/雪崩
- **RabbitMQ + 死信队列** 邮件失败自动重试
- **钉钉机器人** HMAC 加签 Webhook 推送
- 客户查询 **165ms → 69ms，TPS 447**
- Feign + Sentinel + Resilience4j **三层容错体系**
