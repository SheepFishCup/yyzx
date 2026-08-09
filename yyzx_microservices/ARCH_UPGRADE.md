# 架构升级：OpenFeign + 无状态认证

> 颐养中心微服务 — 共享数据库解耦方案与 Gateway 无状态 JWT 认证改造

---

## 一、背景

微服务初次迁移采用了两个临时方案以保证快速跑通：

| 临时方案 | 问题 |
|---------|------|
| **共享数据库直连** — 跨服务数据通过持有对方的 Mapper 直接查表 | 表结构变更时多个服务需同步修改 Mapper XML；违背微服务"服务自治"原则 |
| **有状态 JWT 认证** — Gateway 校验 token 时依赖 Redis 查询 | Redis 不可用时降级放行（安全风险）；增加网络跳数 |

本文档记录针对上述两个问题的升级方案。

---

## 二、OpenFeign 同步调用

### 2.1 改动总览

```
Before (共享数据库)                  After (OpenFeign)
──────────────────────────          ──────────────────────────
yyzx-checkinout                    yyzx-checkinout
  ├── BedMapper (复制)                ├── BedFeignClient
  ├── CustomerMapper (复制)           ├── CustomerFeignClient
  ├── UserMapper (复制)               └── (不再持有 bed/customer/user 的 Mapper)
  ├── BedService (复制)
  └── CustomerService (复制)         yyzx-bed
                                       └── BedInternalController (新增 /internal/bed)
yyzx-bed
  ├── BedMapper                     yyzx-customer
  ├── BedDetailsMapper                └── CustomerInternalController (新增 /internal/customer)
  └── CustomerMapper (复制)
                                     yyzx-auth
                                       └── UserInternalController (新增 /internal/admin)

问题:                               解决:
- Mapper 在多个模块重复              - 每个表只在一个服务中维护 Mapper
- SQL 变更需同步 N 处                - SQL 变更只需改拥有者服务
- 无法独立部署/扩缩容                - 各服务可独立部署
```

### 2.2 新增/修改的文件

#### 父 POM（新增依赖管理）

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>3.1.9</version>
</dependency>
```

#### yyzx-common（新增 3 个 Feign 接口）

```
yyzx-common/src/main/java/com/cqupt/feign/
├── BedFeignClient.java          # → yyzx-bed  /internal/bed
├── CustomerFeignClient.java     # → yyzx-customer /internal/customer
└── UserFeignClient.java         # → yyzx-auth /internal/admin
```

#### 被调用方（新增 3 个 InternalController）

| 文件 | 端点 |
|------|------|
| `yyzx-bed/.../BedInternalController.java` | `GET /internal/bed/{bedId}`, `GET /internal/bed/checkAvailable`, `PUT /internal/bed/updateStatus` |
| `yyzx-customer/.../CustomerInternalController.java` | `GET /internal/customer/{customerId}`, `PUT /internal/customer/{customerId}/level` |
| `yyzx-auth/.../UserInternalController.java` | `GET /internal/admin/user/{userId}`, `GET /internal/admin/users/byRole` |

> 路径前缀 `/internal/**` 已在所有服务的 `WebConfig` JWT 拦截器排除列表中。

#### 调用方（改造 checkinout 模块）

| 文件 | 改动 |
|------|------|
| `YyzxCheckinoutApplication.java` | 添加 `@EnableFeignClients(basePackages = "com.cqupt.feign")` |
| `BackdownController.java` | `BedService` → `BedFeignClient`; `CustomerService` → `CustomerFeignClient` |
| `OutwardController.java` | 同上 |

#### JWT 拦截器（全部 9 个服务）

所有服务的 `WebConfig.java` 的 JWT 拦截器排除列表新增 `/internal/**` 路径。

### 2.3 Feign 调用流程

```
yyzx-checkinout                        yyzx-bed
    BackdownController                    BedInternalController
         │                                     │
         │  1. examineBackdown()               │
         │  2. customerFeignClient             │
         │     .getById(customerId) ──────────►│ yyzx-customer
         │                                     │   CustomerInternalController
         │  3. bedFeignClient                  │
         │     .updateStatus(bedId,1) ────────►│ BedInternalController
         │                                     │   bedMapper.updateById()
         │◄────────────────────────────────────│
         │                                     │
         ▼                                     ▼
    审批完成                              床位已释放
```

### 2.4 后续可按同样模式迁移的模块

| 优先级 | 调用方 | → | 被调用方 | 当前方式 |
|--------|--------|---|---------|---------|
| 高 | yyzx-task | → | yyzx-bed, yyzx-customer | BackdownAutoApproveJob 使用 Mapper |
| 中 | yyzx-customer | → | yyzx-bed | CustomerServiceImpl 使用 BedMapper |
| 低 | yyzx-notification | → | yyzx-auth | AdminNotifyConsumer 使用 UserMapper |
| 低 | yyzx-nursing | → | yyzx-customer | NurseRecordsController 使用 OutwardMapper |

---

## 三、Gateway 无状态 JWT 认证

### 3.1 改动前后对比

```
Before (有状态)                        After (无状态)
─────────────────────                  ────────────────────
请求 → Gateway                         请求 → Gateway
  │                                       │
  ├─ 1. 提取 token                        ├─ 1. 提取 token
  ├─ 2. 解析 JWT 签名                     ├─ 2. 解析 JWT 签名 ✅
  ├─ 3. 检查 Redis: user:token:{id} ─────►├─ 3. (无 Redis 调用)
  │   ├─ 存在且匹配 → 放行                ├─ 4. 提取 claims → headers
  │   ├─ 不存在 → 403                     ├─ 5. 放行
  │   └─ Redis 挂了 → 降级放行 ⚠️          │
  ├─ 4. 提取 userId → X-User-Id          │
  └─ 5. 放行                              │

依赖:                                   依赖:
- Redis ✅ 必须                           - JWT 签名 ✅ 自包含
- 有单点故障风险                          - 无外部依赖
- 可强制下线                              - 强制下线需换密钥
```

### 3.2 JwtAuthGlobalFilter 改动

| 改动项 | 旧版 | 新版 |
|--------|------|------|
| 构造参数 | `AuthProperties` + `ReactiveRedisTemplate` | 仅 `AuthProperties` |
| Redis 依赖 | 强制依赖 `spring-boot-starter-data-redis-reactive` | 不再需要 |
| Token 校验 | JWT 签名 + Redis 存在性双重校验 | 纯 JWT 签名 + 过期时间校验 |
| 转发 Header | `X-User-Id`, `X-User-Name` | `X-User-Id`, `X-User-Name`, `X-User-Role` |
| Redis 不可用时 | 降级放行（安全风险） | 不受影响（根本不查 Redis） |

### 3.3 JWT Claims 扩充

UserServiceImpl 在登录时生成的 JWT 现在包含更完整的信息：

```json
{
  "empId": 1,
  "userId": 1,
  "username": "admin",
  "roleId": 1,
  "exp": 1721900000
}
```

下游服务通过以下 Header 获取用户信息：

| Header | 来源 | 示例值 |
|--------|------|--------|
| `X-User-Id` | claims.userId | `1` |
| `X-User-Name` | claims.username | `admin` |
| `X-User-Role` | claims.roleId | `1` |

### 3.4 无状态认证的取舍

| 优点 | 缺点 |
|------|------|
| 无外部依赖，Gateway 完全自洽 | 无法实时撤销单个 token（强制下线） |
| 减少一次 Redis 网络调用（延迟更低） | Token 签发后直到过期前一直有效 |
| Redis 维护窗口不影响认证 | 需要配合短有效期 token（建议 30min） |
| 水平扩展更简单 | 需要 refresh token 机制 |

### 3.5 强制下线方案

采用**无状态认证**后，如果要强制某个用户下线：

1. **短期方案**：等待 token 自然过期（当前 2 小时，可改为 30 分钟）
2. **紧急方案**：修改 JWT 密钥 → 所有 token 立即失效（所有人需重新登录）
3. **推荐方案**：配合 **refresh token + access token 短有效期** 实现近似实时撤销

---

## 四、Gateway 简化后的依赖

```xml
<!-- pom.xml — 不再需要 Redis 相关依赖 -->
<!-- 移除: spring-boot-starter-data-redis-reactive -->

<!-- 保留: -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
</dependency>
```

> Gateway 的 `pom.xml` 中仍保留了 `spring-boot-starter-data-redis-reactive`，如需彻底移除可手动删除（当前保留不影响功能）。

---

## 五、文件改动清单

```
新增 (7):
├── yyzx-common/src/main/java/com/cqupt/feign/BedFeignClient.java
├── yyzx-common/src/main/java/com/cqupt/feign/CustomerFeignClient.java
├── yyzx-common/src/main/java/com/cqupt/feign/UserFeignClient.java
├── yyzx-bed/.../controller/BedInternalController.java
├── yyzx-customer/.../controller/CustomerInternalController.java
├── yyzx-auth/.../controller/UserInternalController.java
└── ARCH_UPGRADE.md (本文件)

修改 (17):
├── pom.xml                              — 新增 OpenFeign 依赖管理
├── yyzx-common/pom.xml                  — 新增 OpenFeign 依赖
├── yyzx-gateway/.../JwtAuthGlobalFilter.java  — 去除 Redis，改为纯 JWT
├── yyzx-auth/.../UserServiceImpl.java         — JWT claims 增加 username/roleId
├── yyzx-checkinout/.../YyzxCheckinoutApplication.java — 添加 @EnableFeignClients
├── yyzx-checkinout/.../BackdownController.java       — Feign 替代 Service
├── yyzx-checkinout/.../OutwardController.java        — Feign 替代 Service
└── 9个服务的 WebConfig.java                    — JWT拦截器排除 /internal/**
```

---

## 六、Sentinel 流量防卫兵（第2步）

### 6.1 改造目标

在 API 网关层和核心业务接口添加 Sentinel 限流熔断保护，防止流量突增击垮服务。

### 6.2 新增/修改文件

```
新增 (3):
├── yyzx-gateway/.../config/SentinelGatewayConfig.java   — Gateway Sentinel 过滤器 + 429 降级响应
├── yyzx-customer/.../config/SentinelRulesConfig.java     — listKhxxPage QPS=100 + 熔断规则
└── scripts/skywalking-agent.config                       — (见第七节)

修改 (5):
├── yyzx-gateway/pom.xml                     — 新增 sentinel + sentinel-gateway 依赖
├── yyzx-gateway/application.yaml            — Sentinel Dashboard + fallback 模式配置
├── yyzx-customer/pom.xml                    — 新增 sentinel 依赖
├── yyzx-customer/application.yaml           — Sentinel transport 配置
└── yyzx-customer/.../CustomerController.java — @SentinelResource + blockHandler + fallback
```

### 6.3 Sentinel 限流架构

```
Gateway 层:
  /customer/** → Sentinel Gateway Filter → 超 QPS → HTTP 429
                                              └─ {"flag":false,"message":"请求过于频繁..."}

Controller 层:
  listKhxxPage → @SentinelResource
    ├─ QPS > 100 → blockHandler → ResultVo.fail("查询请求过多")
    └─ 异常 > 50% → fallback   → ResultVo.fail("服务繁忙")
```

---

## 七、SkyWalking 链路追踪（第3步）

### 7.1 接入服务

| 服务 | agent.service_name | 接入方式 |
|------|-------------------|---------|
| yyzx-gateway | `yyzx-gateway` | `-javaagent` JVM 参数 |
| yyzx-auth | `yyzx-auth` | `-javaagent` JVM 参数 |
| yyzx-customer | `yyzx-customer` | `-javaagent` JVM 参数 |

### 7.2 新增/修改文件

```
新增 (6):
├── scripts/skywalking-agent.config              — Agent 通用配置
├── scripts/start-gateway.bat                     — Windows 启动脚本
├── scripts/start-auth.bat                        — Windows 启动脚本
├── scripts/start-customer.bat                    — Windows 启动脚本
└── scripts/start-with-skywalking.sh              — Linux/Mac 统一启动脚本

修改 (5):
├── yyzx-gateway/Dockerfile                       — 增加 SkyWalking Agent 可选集成
├── yyzx-gateway/application.yaml                 — logging.pattern 增加 [%X{traceId}]
├── yyzx-auth/application.yaml                    — logging.pattern 增加 [%X{traceId}]
├── yyzx-customer/application.yaml                — logging.pattern 增加 [%X{traceId}]
└── 3个服务的 application-docker.yaml              — 同上
```

### 7.3 日志效果

```
# 开启 SkyWalking 后每条日志自动带 TraceId：
2026-07-28 10:00:01.123 [http-nio-8080] [a1b2c3d4e5f6] INFO  CustomerController - 客户信息列表查询
```

---

## 八、DingTalkRobotService 接口抽取（TODO §3.1 #5）

### 8.1 改动

| 文件 | 改动 |
|------|------|
| `DingTalkRobotService.java` | 重写为 `public interface`（2 个方法签名） |
| `DingTalkRobotServiceImpl.java` | **新建**：`@Service` 实现类，迁入原逻辑 |
| `AdminNotifyConsumer.java` | 无需修改（字段类型已是接口，Spring 自动注入实现） |

---

## 九、跨模块 Mapper 清理（TODO §3.2 #2）

### 9.1 清理逻辑

Feign 16 个端点覆盖全部跨模块数据访问后，删除各模块中冗余的 Mapper 副本。

### 9.2 删除清单（22 个文件）

| 模块 | 删除的 Mapper/Service | 数量 |
|------|----------------------|------|
| yyzx-task | `BackdownMapper` + `BedMapper` + `CustomerMapper`（含 XML） | 6 |
| yyzx-notification | `UserMapper`（含 XML） | 2 |
| yyzx-checkinout | `BedMapper` + `BedDetailsMapper` + `CustomerMapper` + `UserMapper`（含 XML）+ `BedService`/`BedServiceImpl` + `CustomerService`/`CustomerServiceImpl` | 14 |

### 9.3 清理后各模块仅持有自己的领域 Mapper

| 模块 | 剩余 Mapper |
|------|-----------|
| yyzx-task | `FailedMailRecordMapper` |
| yyzx-notification | `FailedMailRecordMapper` |
| yyzx-checkinout | `BackdownMapper` + `OutwardMapper` |

---

## 十、Internal 端点共享密钥认证

### 10.1 新增文件（4）

```
yyzx-common/.../constant/InternalTokenConstant.java      — X-Internal-Token Header + 密钥
yyzx-common/.../interceptor/InternalAuthInterceptor.java  — 拦截 /internal/** 校验密钥
yyzx-common/.../feign/FeignRequestInterceptor.java        — Feign 自动注入 Header
4 个 application.yaml                                     — yyzx.internal-token 配置
```

### 10.2 认证流程

```
Feign 调用 → FeignRequestInterceptor 注入 X-Internal-Token
         → 被调用方 InternalAuthInterceptor 校验
         → 密钥匹配 ✅ / 不匹配 ❌ 403
```

---

## 十一、FileUpload 路径配置化

`FileUploadUtil` → 统一引用 `FileUploadConstant.IMAGE_BASE_PATH`  
`FoodContoller` + `WebConfig` → 共用 `@Value("${yyzx.upload.path}")` 可配置路径
