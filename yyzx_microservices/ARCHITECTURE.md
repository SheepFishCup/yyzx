# 项目架构总览

> 颐养中心（yyzx）微服务架构文档  
> 由原单体 Spring Boot 项目 `yyzx_backend` 拆分而来

---

## 1. 架构图

```mermaid
graph TB
    subgraph 客户端
        WEB[Vue3 前端<br/>yyzx_front]
        WX[微信小程序]
    end

    subgraph 网关层
        GW[yyzx-gateway<br/>Spring Cloud Gateway<br/>:8080<br/>JWT无状态认证<br/>Sentinel限流<br/>SkyWalking追踪]
    end

    subgraph 注册中心 & 配置中心
        NACOS[Nacos 2.2.3<br/>:8848]
    end

    subgraph 可观测性
        SW[SkyWalking OAP<br/>:11800 :12800]
        SENTINEL[Sentinel Dashboard<br/>:8080]
    end

    subgraph 业务服务
        AUTH[yyzx-auth<br/>:8081<br/>认证/用户/角色]
        CUST[yyzx-customer<br/>:8082<br/>客户/偏好/小程序<br/>Sentinel @Resource]
        BED[yyzx-bed<br/>:8083<br/>床位/房间]
        NURSE[yyzx-nursing<br/>:8084<br/>护理项目/等级/记录]
        CIO[yyzx-checkinout<br/>:8085<br/>退住/外出审批<br/>Feign+Fallback]
        MEAL[yyzx-meal<br/>:8086<br/>菜品/膳食]
        RPT[yyzx-report<br/>:8088<br/>统计报表/Excel导出]
    end

    subgraph 后台服务
        NOTIF[yyzx-notification<br/>:8087<br/>邮件/钉钉/WebSocket<br/>RabbitMQ消费者]
        TASK[yyzx-task<br/>:8089<br/>Quartz定时任务]
    end

    subgraph 中间件
        MQ[RabbitMQ 3.12<br/>:5672 :15672]
        REDIS[Redis 7<br/>:6379]
        DB[(MySQL 8.0<br/>:3306<br/>数据库 yyzx)]
    end

    subgraph 公共模块
        COMMON[yyzx-common<br/>实体/DTO/VO/工具类]
    end

    WEB --> GW
    WX --> GW
    GW --> NACOS
    GW --> REDIS
    GW --> AUTH
    GW --> CUST
    GW --> BED
    GW --> NURSE
    GW --> CIO
    GW --> MEAL
    GW --> RPT

    AUTH --> DB
    AUTH --> REDIS
    AUTH --> MQ
    CUST --> DB
    CUST --> REDIS
    CUST --> MQ
    BED --> DB
    BED --> REDIS
    NURSE --> DB
    NURSE --> REDIS
    CIO --> DB
    MEAL --> DB
    MEAL --> REDIS
    RPT --> DB
    NOTIF --> MQ
    NOTIF --> DB
    NOTIF --> REDIS
    TASK --> DB
    TASK --> REDIS
    TASK --> MQ

    AUTH -.-> COMMON
    CUST -.-> COMMON
    BED -.-> COMMON
    NURSE -.-> COMMON
    CIO -.-> COMMON
    MEAL -.-> COMMON
    RPT -.-> COMMON
    NOTIF -.-> COMMON
    TASK -.-> COMMON
```

---

## 2. 模块列表

| 序号 | 模块 | 端口 | 打包 | 说明 |
|------|------|------|------|------|
| 0 | `yyzx-common` | — | jar | 公共模块：实体类、DTO、VO、工具类、常量、异常、RabbitMQ生产者 |
| 1 | `yyzx-gateway` | 8080 | jar | API 网关：Spring Cloud Gateway + JWT 全局认证过滤器 |
| 2 | `yyzx-auth` | 8081 | jar | 认证服务：登录、验证码、用户管理、角色菜单（RBAC） |
| 3 | `yyzx-customer` | 8082 | jar | 客户服务：客户CRUD、偏好管理、护理项目分配、微信小程序登录 |
| 4 | `yyzx-bed` | 8083 | jar | 床位服务：床位CRUD、房间管理、床位交换 |
| 5 | `yyzx-nursing` | 8084 | jar | 护理服务：护理项目、护理等级、护理记录（Redis Lua 库存扣减） |
| 6 | `yyzx-checkinout` | 8085 | jar | 出入管理：退住审批、外出审批 |
| 7 | `yyzx-meal` | 8086 | jar | 膳食服务：菜品管理（含图片上传）、膳食计划 |
| 8 | `yyzx-notification` | 8087 | jar | 通知服务：RabbitMQ 消费者（邮件/通知/日志/管理员通知/死信队列）、钉钉机器人、WebSocket 推送 |
| 9 | `yyzx-report` | 8088 | jar | 报表服务：客户入住统计、财务统计、Excel 模板导出 |
| 10 | `yyzx-task` | 8089 | jar | 定时任务：Quartz（退住自动审批、失败邮件重试、延迟队列、周报） |

---

## 3. 技术栈版本

| 类别 | 技术 | 版本 |
|------|------|------|
| **语言** | Java | 1.8 |
| **框架** | Spring Boot | 2.7.18 |
| **服务调用** | OpenFeign + Resilience4j | 3.1.9 / 2.1.7 |
| **流量防卫** | Sentinel | (via SCA 2021.0.6.1) |
| **链路追踪** | SkyWalking Java Agent | 9.x |
| **微服务** | Spring Cloud | 2021.0.9 |
| **微服务** | Spring Cloud Alibaba | 2021.0.6.1 |
| **网关** | Spring Cloud Gateway | (via SC 2021.0.9) |
| **注册/配置** | Nacos | 2.2.3 |
| **负载均衡** | Spring Cloud LoadBalancer | (via SC 2021.0.9) |
| **ORM** | MyBatis-Plus | 3.5.1 |
| **连接池** | Druid | 1.2.6 |
| **数据库** | MySQL | 8.0.11 |
| **缓存** | Redis + Redisson | 7.x / 3.17.7 |
| **消息队列** | RabbitMQ | 3.12 |
| **定时任务** | Quartz | 2.3.2 |
| **认证** | JJWT + Auth0 JWT | 0.9.1 / 3.10.3 |
| **密码加密** | Spring Security Crypto (BCrypt) | 5.7.6 |
| **API 文档** | Knife4j (OpenAPI 3) | 4.4.0 |
| **JSON** | Fastjson | 1.2.76 |
| **Excel** | Apache POI | 3.16 |
| **验证码** | Kaptcha | 2.3.2 |
| **HTTP 客户端** | Apache HttpClient | 4.5.13 |
| **邮件** | Spring Boot Mail + Thymeleaf | managed |
| **WebSocket** | Spring Boot WebSocket | managed |
| **钉钉** | DingTalk SDK | 2.0.0 |
| **构建** | Maven | 3.6.3+ |

---

## 4. 服务间依赖关系

```mermaid
graph LR
    GW[yyzx-gateway] -->|路由| AUTH
    GW -->|路由| CUST
    GW -->|路由| BED
    GW -->|路由| NURSE
    GW -->|路由| CIO
    GW -->|路由| MEAL
    GW -->|路由| RPT

    AUTH -->|RabbitMQ 消息| MQ
    CUST -->|RabbitMQ 消息| MQ
    NOTIF -->|消费消息| MQ
    TASK -->|RabbitMQ 消息| MQ

    AUTH -->|共享数据库| DB
    CUST -->|共享数据库| DB
    BED -->|共享数据库| DB
    NURSE -->|共享数据库| DB
    CIO -->|共享数据库| DB
    MEAL -->|共享数据库| DB
    RPT -->|共享数据库| DB
    NOTIF -->|共享数据库| DB
    TASK -->|共享数据库| DB

    AUTH -->|依赖| COMMON[yyzx-common]
    CUST -->|依赖| COMMON
    BED -->|依赖| COMMON
    NURSE -->|依赖| COMMON
    CIO -->|依赖| COMMON
    MEAL -->|依赖| COMMON
    RPT -->|依赖| COMMON
    NOTIF -->|依赖| COMMON
    TASK -->|依赖| COMMON
```

### 依赖说明

| 依赖类型 | 说明 |
|---------|------|
| **路由依赖** | Gateway 通过 Nacos 发现服务，使用 `lb://` 负载均衡转发请求 |
| **消息依赖** | 业务服务通过 `RabbitMQProducerService`（common）发送消息，`yyzx-notification` 统一消费 |
| **数据库依赖** | 共享数据库，各模块仅持有自己领域的 Mapper |
| **模块依赖** | 全部服务依赖 `yyzx-common`（Maven） |
| **服务调用** | OpenFeign 16 个端点覆盖全部跨模块调用，`FeignRequestInterceptor` 自动注入内部认证 Token |
| **内部认证** | `InternalAuthInterceptor` 校验 `/internal/**` 共享密钥，防止绕过 Gateway 直接访问 |

---

## 5. 消息队列架构

```mermaid
graph LR
    subgraph 生产者
        AUTH_P[yyzx-auth]
        CUST_P[yyzx-customer]
        TASK_P[yyzx-task]
    end

    subgraph RabbitMQ
        EXCHANGE[yyzx.direct.exchange<br/>DirectExchange]
        MQ_MAIL[yyzx.mail.queue<br/>TTL 5min]
        MQ_NOTIFY[yyzx.notify.queue]
        MQ_LOG[yyzx.log.queue]
        MQ_ADMIN[yyzx.admin.notify.queue]
        DLX[yyzx.dlx.exchange<br/>死信交换机]
        DLX_MAIL[yyzx.mail.dlx.queue]
    end

    subgraph 消费者
        MAIL_C[MailConsumer<br/>发送邮件]
        NOTIFY_C[NotifyConsumer<br/>推送通知]
        LOG_C[LogConsumer<br/>记录日志]
        ADMIN_C[AdminNotifyConsumer<br/>钉钉群通知]
        DLX_C[MailDlxConsumer<br/>失败邮件记录]
    end

    AUTH_P -->|sendMail/notify/log/adminNotify| EXCHANGE
    CUST_P -->|sendMail/notify/log/adminNotify| EXCHANGE
    TASK_P -->|sendMail/notify/log/adminNotify| EXCHANGE

    EXCHANGE --> MQ_MAIL
    EXCHANGE --> MQ_NOTIFY
    EXCHANGE --> MQ_LOG
    EXCHANGE --> MQ_ADMIN

    MQ_MAIL -->|过期| DLX
    DLX --> DLX_MAIL

    MAIL_C --> MQ_MAIL
    NOTIFY_C --> MQ_NOTIFY
    LOG_C --> MQ_LOG
    ADMIN_C --> MQ_ADMIN
    DLX_C --> DLX_MAIL
```

---

## 6. 认证流程

```mermaid
sequenceDiagram
    participant FE as 前端
    participant GW as Gateway :8080
    participant Redis as Redis
    participant SVC as 业务服务

    FE->>GW: POST /admin/loginWithCaptcha<br/>(用户名+密码+验证码)
    GW->>SVC: 路由到 yyzx-auth
    SVC->>Redis: 校验验证码
    SVC->>SVC: BCrypt 密码验证
    SVC->>Redis: 存储 token (user:token:{userId})
    SVC-->>GW: 返回 JWT token
    GW-->>FE: {flag:true, data:{token:"xxx"}}

    Note over FE,SVC: --- 后续请求 ---

    FE->>GW: GET /customer/listKhxxPage<br/>Header: token=xxx
    GW->>GW: JwtAuthGlobalFilter（无状态）<br/>1.白名单检查 2.解析JWT签名 3.提取claims
    GW->>SVC: Header: X-User-Id={userId}<br/>X-User-Name={username}<br/>路由到 yyzx-customer
    SVC-->>GW: 响应数据
    GW-->>FE: JSON 响应
```

---

## 7. 高可用容错架构

```
请求: GET /customer/listKhxxPage
        │
        ├── [SkyWalking] TraceId 注入 ──────── 贯穿全链路
        │
        ├── [Sentinel Gateway] QPS 限流 ───── 超阈值 → HTTP 429
        │
        ├── [JWT Filter] 无状态签名校验 ───── 失败 → HTTP 401
        │
        ├── [Sentinel @Resource] listKhxxPage ─ 限流 → blockHandler
        │                                      ─ 异常 → fallback
        │
        └── [Feign + Resilience4j] 调用床位服务 ─ 超时/失败 → FallbackFactory
                                                  └─ 返回 ResultVo.fail("服务暂不可用")
```

### 熔断降级参数

| 层级 | 机制 | 参数 |
|------|------|------|
| Gateway | Sentinel 限流 | QPS 阈值（Dashboard 动态配置） |
| Controller | `@SentinelResource` | QPS=100 + 异常比例 50% 熔断 10s |
| Feign | Resilience4j FallbackFactory | 超时 5s，50% 失败率熔断，10s 半开 |
| 数据库 | Hikari/Druid 连接池 | 10~25 连接，3s 超时 |
| Internal API | InternalAuthInterceptor | X-Internal-Token 共享密钥 |

---

## 8. 安全架构

```
外部请求:
  浏览器 → Gateway (:8080) → JWT 无状态认证 → 路由到服务

内部 Feign 调用:
  Service A → FeignRequestInterceptor 注入 X-Internal-Token
            → Service B /internal/** → InternalAuthInterceptor 校验
            → ✅ 放行 / ❌ 403
```
