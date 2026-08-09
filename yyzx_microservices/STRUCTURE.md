# 项目目录结构

> 颐养中心微服务完整目录树（`yyzx_microservices/`）

---

## 根目录

```
yyzx_microservices/
├── pom.xml                          # Maven 父工程（11个子模块 + 统一依赖管理）
├── docker-compose.yml               # Docker Compose 编排（14个容器）
├── .env                             # 环境变量（供 docker-compose 使用）
├── README.md                        # 项目说明
├── ARCHITECTURE.md                  # 架构总览
├── STRUCTURE.md                     # 本文件
├── nacos-config/                    # Nacos 配置中心导入文件（11个）
│   ├── yyzx-common.yaml             #   共享配置（所有服务引用）
│   ├── yyzx-gateway.yaml            #   网关路由配置
│   ├── yyzx-auth.yaml               #   认证服务配置
│   ├── yyzx-customer.yaml           #   客户服务配置
│   ├── yyzx-bed.yaml                #   床位服务配置
│   ├── yyzx-nursing.yaml            #   护理服务配置
│   ├── yyzx-checkinout.yaml         #   出入管理配置
│   ├── yyzx-meal.yaml               #   膳食服务配置
│   ├── yyzx-notification.yaml       #   通知服务配置
│   ├── yyzx-report.yaml             #   报表服务配置
│   └── yyzx-task.yaml               #   定时任务配置
│
├── yyzx-common/                     # [0] 公共模块 — jar 包
├── yyzx-gateway/                    # [1] API 网关 — :8080 (Sentinel限流+SkyWalking追踪)
├── yyzx-auth/                       # [2] 认证服务 — :8081 (SkyWalking追踪)
├── yyzx-customer/                   # [3] 客户服务 — :8082 (Sentinel限流+SkyWalking追踪)
├── yyzx-bed/                        # [4] 床位服务 — :8083
├── yyzx-nursing/                    # [5] 护理服务 — :8084
├── yyzx-checkinout/                 # [6] 出入管理 — :8085 (Feign+Resilience4j熔断)
├── yyzx-meal/                       # [7] 膳食服务 — :8086
├── yyzx-notification/               # [8] 通知服务 — :8087
├── yyzx-report/                     # [9] 报表服务 — :8088
├── yyzx-task/                       # [10] 定时任务 — :8089 (Feign调用)
└── scripts/                         # [11] 启动脚本与Agent配置
```

---

## yyzx-common — 公共模块 (91 Java, jar)

```
yyzx-common/
├── pom.xml
└── src/main/java/com/cqupt/
    ├── annotation/          # 自定义注解 (2)
    │   ├── File.java                    # 文件校验注解
    │   └── MultiFieldAssociationCheck.java  # 多字段关联校验注解
    ├── constant/            # 常量类 (9)
    │   ├── InternalTokenConstant.java    # ★ 内部服务认证密钥
    │   ├── FileUploadConstant.java
    │   ├── JwtClaimsConstant.java
    │   ├── MessageConstant.java
    │   ├── PasswordConstant.java
    │   ├── RabbitMQConstant.java        # ★ RabbitMQ 队列/交换机常量（新增）
    │   ├── RedisConstant.java
    │   ├── StatusConstant.java
    │   └── WeChatConstant.java
    ├── context/             # 上下文 (1)
    │   └── BaseContext.java             # ThreadLocal 用户上下文
    ├── dto/                 # 数据传输对象 (18)
    │   ├── BackdownDTO.java
    │   ├── BedDetailsDTO.java
    │   ├── CustomerNurseItemDTO.java
    │   ├── CustomerPreferenceDTO.java
    │   ├── ExchangeDTO.java
    │   ├── ForgotPasswordDTO.java
    │   ├── ImageCodeDTO.java
    │   ├── KhxxDTO.java
    │   ├── LogMessage.java              # RabbitMQ 日志消息
    │   ├── LoginWithCodeDTO.java
    │   ├── MailMessage.java             # RabbitMQ 邮件消息
    │   ├── MealDTO.java
    │   ├── NotifyMessage.java           # RabbitMQ 通知消息
    │   ├── NurseItemDTO.java
    │   ├── NurseRecordDTO.java
    │   ├── OutwardDTO.java
    │   ├── ResetPasswordDTO.java
    │   └── UserDTO.java
    ├── exception/           # 异常类 (8)
    │   ├── AccountLockedException.java
    │   ├── AccountNotFoundException.java
    │   ├── BaseException.java
    │   ├── BusinessException.java
    │   ├── DeletionNotAllowedException.java
    │   ├── LoginFailedException.java
    │   ├── NurseitemException.java
    │   └── PasswordErrorException.java
    ├── handler/             # 处理器 (2)
    │   ├── GlobalExceptionHandler.java  # 全局异常处理
    │   └── MyMetaObjectHandler.java     # MyBatis-Plus 自动填充
    ├── interceptor/         # 拦截器 (2)
    │   ├── JwtTokenInterceptor.java     # JWT 拦截器（Gateway 场景下可选）
    │   └── InternalAuthInterceptor.java  # ★ /internal/** 共享密钥认证
    ├── pojo/                # 实体类 (20)
    │   ├── Backdown.java
    │   ├── Bed.java
    │   ├── BedDetails.java
    │   ├── Customer.java
    │   ├── CustomerNurseItem.java
    │   ├── CustomerPreference.java
    │   ├── FailedMailRecord.java
    │   ├── Food.java
    │   ├── Meal.java
    │   ├── Menu.java
    │   ├── NurseContent.java
    │   ├── NurseLevel.java
    │   ├── NurseLevelItem.java
    │   ├── NurseRecord.java
    │   ├── Outward.java
    │   ├── Patient.java
    │   ├── Role.java
    │   ├── RoleMenu.java
    │   ├── Room.java
    │   └── User.java
    ├── properties/          # 配置属性 (2)
    │   ├── JwtProperties.java
    │   └── WeChatProperties.java
    ├── rabbit/              # RabbitMQ (1)
    │   └── RabbitMQProducerService.java # ★ 统一消息发送服务（供所有业务模块使用）
    └── feign/               # OpenFeign 客户端接口 + 降级工厂 + 请求拦截器 (9)
        ├── BedFeignClient.java
        ├── BedFeignClientFallbackFactory.java
        ├── CustomerFeignClient.java
        ├── CustomerFeignClientFallbackFactory.java
        ├── UserFeignClient.java
        ├── UserFeignClientFallbackFactory.java
        ├── BackdownFeignClient.java
        ├── BedFeignClientFallbackFactory.java  (BedDetails)
        └── FeignRequestInterceptor.java         # ★ Feign 自动注入 Internal Token
        ├── BedFeignClient.java              # → yyzx-bed (含 BedDetailsFeignClient)
        ├── BedFeignClientFallbackFactory.java  # Bed Feign 熔断降级
        ├── CustomerFeignClient.java          # → yyzx-customer
        ├── CustomerFeignClientFallbackFactory.java
        ├── UserFeignClient.java              # → yyzx-auth
        ├── UserFeignClientFallbackFactory.java
        ├── BackdownFeignClient.java          # → yyzx-checkinout
        └── BedFeignClientFallbackFactory.java
    ├── utils/               # 工具类 (12)
    │   ├── CustomDateDeserializer.java
    │   ├── DateConverter.java
    │   ├── ExcelTemplateGenerator.java
    │   ├── FileUploadUtil.java
    │   ├── HttpClientUtil.java
    │   ├── HybridBlacklistUtils.java    # 布隆过滤器 + Redis Set 黑名单
    │   ├── ImageCodeUtil.java           # Kaptcha 验证码
    │   ├── JwtUtil.java                 # JWT 创建/解析
    │   ├── RedisDelayQueueUtils.java    # Redis ZSET 延迟队列
    │   ├── RedisLuaUtils.java           # Redis Lua 脚本执行器
    │   ├── ResultVo.java                # 统一响应对象
    │   └── WeChatPayUtil.java
    ├── valid/groups/        # 校验分组 (2)
    │   ├── Add.java
    │   └── Update.java
    ├── validator/           # 校验器 (2)
    │   ├── FileValidator.java
    │   └── MultiFieldAssociationCheckValidator.java
    └── vo/                  # 视图对象 (12)
        ├── BackdownVo.java
        ├── BedDetailsVo.java
        ├── CustomerNurseItemVo.java
        ├── CustomerPreferenceVo.java
        ├── CustomerStatsVo.java
        ├── CwsyBedVo.java
        ├── FinanceStatsVo.java
        ├── KhxxCustomerVo.java
        ├── MealVo.java
        ├── NurseRecordsVo.java
        ├── NursingLevelDistVo.java
        └── OutwardVo.java
```

---

## yyzx-gateway — API 网关 (:8080)

```
yyzx-gateway/
├── pom.xml                     # 依赖 spring-cloud-starter-gateway（WebFlux）
├── Dockerfile
└── src/
    ├── main/java/com/cqupt/gateway/
    │   ├── YyzxGatewayApplication.java
    │   ├── config/
    │   │   └── GatewayCorsConfig.java      # WebFlux CORS 全局配置
    │   └── filter/
    │       ├── AuthProperties.java         # JWT 白名单配置
    │       └── JwtAuthGlobalFilter.java    # ★ JWT 全局认证过滤器
    ├── main/resources/
    │   ├── application.yaml               # 22条路由规则
    │   └── application-docker.yaml
    └── test/java/com/cqupt/gateway/
        └── YyzxGatewayApplicationTests.java
```

---

## yyzx-auth — 认证服务 (:8081)

```
yyzx-auth/
├── pom.xml + Dockerfile
└── src/
    ├── main/java/com/cqupt/auth/
    │   ├── YyzxAuthApplication.java
    │   ├── config/      (6) — Redis, Redisson, MyBatisPlus, Druid, Swagger, Web
    │   ├── controller/  (4) — Admin, Role, Menu, RoleMenu
    │   ├── mapper/      (4) — User, Role, Menu, RoleMenu
    │   └── service/     (4接口+4实现)
    ├── main/resources/
    │   ├── application.yaml / application-docker.yaml
    │   └── mapper/      (4 XML)
    └── test/            (1 测试)
```

---

## yyzx-customer — 客户服务 (:8082)

```
yyzx-customer/
├── pom.xml + Dockerfile
└── src/
    ├── main/java/com/cqupt/customer/
    │   ├── YyzxCustomerApplication.java
    │   ├── config/      (6) — Redis, Redisson, MyBatisPlus, Druid, Swagger, Web
    │   ├── controller/  (4) — Customer, CustomerNurseItem, CustomerPreference, Patient
    │   ├── mapper/      (7) — 含 Bed/BedDetails/User（跨表访问）
    │   └── service/     (4接口+4实现)
    ├── main/resources/
    │   ├── application.yaml / application-docker.yaml
    │   └── mapper/      (7 XML)
    └── test/            (1 测试)
```

---

## yyzx-bed — 床位服务 (:8083)

```
yyzx-bed/
├── pom.xml + Dockerfile
└── src/
    ├── main/java/com/cqupt/bed/
    │   ├── YyzxBedApplication.java
    │   ├── config/      (5) — Redis, MyBatisPlus, Druid, Swagger, Web
    │   ├── controller/  (3) — Bed, BedDetails, Room
    │   ├── mapper/      (4) — 含 CustomerMapper（跨表访问）
    │   └── service/     (3接口+3实现)
    ├── main/resources/
    │   ├── application.yaml / application-docker.yaml
    │   └── mapper/      (4 XML)
    └── test/            (1 测试)
```

---

## yyzx-nursing — 护理服务 (:8084)

```
yyzx-nursing/
├── pom.xml + Dockerfile
└── src/
    ├── main/java/com/cqupt/nursing/
    │   ├── YyzxNursingApplication.java
    │   ├── config/      (6) — Redis, Redisson, MyBatisPlus, Druid, Swagger, Web
    │   ├── controller/  (4) — NurseContent, NurseLevel, NurseLevelItem, NurseRecords
    │   ├── mapper/      (6) — 含 CustomerNurseItem/Outward（跨表访问）
    │   └── service/     (5接口+5实现) — 含 OutwardService
    ├── main/resources/
    │   ├── application.yaml / application-docker.yaml
    │   └── mapper/      (6 XML)
    └── test/            (1 测试)
```

---

## yyzx-checkinout — 出入管理 (:8085)

```
yyzx-checkinout/
├── pom.xml + Dockerfile
└── src/
    ├── main/java/com/cqupt/checkinout/
    │   ├── YyzxCheckinoutApplication.java
    │   ├── config/      (4) — MyBatisPlus, Druid, Swagger, Web
    │   ├── controller/  (2) — Backdown, Outward
    │   ├── mapper/      (5) — 含 Bed/Customer/User（跨表访问）
    │   └── service/     (4接口+4实现) — 含 BedService/CustomerService
    ├── main/resources/
    │   ├── application.yaml / application-docker.yaml
    │   └── mapper/      (5 XML)
    └── test/            (1 测试)
```

---

## yyzx-meal — 膳食服务 (:8086)

```
yyzx-meal/
├── pom.xml + Dockerfile
└── src/
    ├── main/java/com/cqupt/meal/
    │   ├── YyzxMealApplication.java
    │   ├── config/      (5) — Redis, MyBatisPlus, Druid, Swagger, Web
    │   ├── controller/  (2) — FoodContoller, Meal
    │   ├── mapper/      (2) — Food, Meal
    │   └── service/     (2接口+2实现)
    ├── main/resources/
    │   ├── application.yaml / application-docker.yaml
    │   ├── mapper/      (2 XML)
    │   └── static/images/ (10 张食物图片)
    └── test/            (1 测试)
```

---

## yyzx-notification — 通知服务 (:8087)

```
yyzx-notification/
├── pom.xml + Dockerfile
└── src/
    ├── main/java/com/cqupt/notification/
    │   ├── YyzxNotificationApplication.java
    │   ├── config/      (8) — RabbitMQ, Redis, MyBatisPlus, Druid, Swagger, WebSocket, DingTalk, Web
    │   ├── consumer/    (5) — Mail, MailDlx, Notify, AdminNotify, Log
    │   ├── mapper/      (2) — FailedMailRecord, User
    │   ├── service/     (3接口+3实现) — Mail, DingTalkRobot, FailedMail
    │   └── websocket/   (1) — WebSocketServer
    ├── main/resources/
    │   ├── application.yaml / application-docker.yaml
    │   ├── mapper/      (2 XML)
    │   └── templates/   (1) — password-reset-email.html
    └── test/            (1 测试)
```

---

## yyzx-report — 报表服务 (:8088)

```
yyzx-report/
├── pom.xml + Dockerfile
└── src/
    ├── main/java/com/cqupt/report/
    │   ├── YyzxReportApplication.java
    │   ├── config/      (4) — MyBatisPlus, Druid, Swagger, Web
    │   ├── controller/  (1) — ReportController（4个端点）
    │   ├── mapper/      (4) — Customer, Bed, Backdown, CustomerNurseItem
    │   └── service/     (1接口+1实现) — ReportService（重写版）
    ├── main/resources/
    │   ├── application.yaml / application-docker.yaml
    │   ├── mapper/      (4 XML)
    │   └── templates/   (3 Excel模板)
    └── test/            (1 测试)
```

---

## yyzx-task — 定时任务 (:8089)

```
yyzx-task/
├── pom.xml + Dockerfile
└── src/
    ├── main/java/com/cqupt/task/
    │   ├── YyzxTaskApplication.java
    │   ├── config/      (6) — Quartz, Redis, MyBatisPlus, Druid, Swagger, Web
    │   ├── job/         (4) — BackdownAutoApprove（重写）, DelayQueueProcessor（重写）,
    │   │                       FailedMailRetry, GenerateWeeklyReport
    │   ├── mapper/      (4) — Backdown, Bed, Customer, FailedMailRecord
    │   └── service/     (1接口+1实现) — FailedMailService
    ├── main/resources/
    │   ├── application.yaml / application-docker.yaml
    │   └── mapper/      (4 XML)
    └── test/            (1 测试)
```

---

---

## scripts — 启动脚本与 Agent 配置

```
scripts/
├── skywalking-agent.config          # SkyWalking Agent 通用配置
├── start-gateway.bat                 # Windows: Gateway + SkyWalking
├── start-auth.bat                    # Windows: Auth + SkyWalking
├── start-customer.bat                # Windows: Customer + SkyWalking
└── start-with-skywalking.sh          # Linux/Mac: gateway|auth|customer|all
```

---

## 文件统计

| 模块 | Java | XML | YAML | 测试 | Dockerfile | 其他 | 合计 |
|------|------|-----|------|------|------------|------|------|
| yyzx-common | 102 | — | — | — | 1 | 1 pom | 104 |
| yyzx-gateway | 6 | — | 2 | 1 | 1 | 1 pom | 11 |
| yyzx-auth | 25 | 4 | 2 | 1 | 1 | 1 pom | 34 |
| yyzx-customer | 25 | 4 | 2 | 1 | 1 | 1 pom | 34 |
| yyzx-bed | 19 | 3 | 2 | 1 | 1 | 1 pom | 27 |
| yyzx-nursing | 23 | 5 | 2 | 1 | 1 | 1 pom | 33 |
| yyzx-checkinout | 15 | 3 | 2 | 1 | 1 | 1 pom | 23 |
| yyzx-meal | 14 | 2 | 2 | 1 | 1 | 1 pom + 图片 | 21 |
| yyzx-notification | 23 | 2 | 2 | 1 | 1 | 1 pom + 模板 | 30 |
| yyzx-report | 12 | 4 | 2 | 1 | 1 | 1 pom + 模板 | 21 |
| yyzx-task | 14 | 1 | 2 | 1 | 1 | 1 pom | 20 |
| scripts | — | — | 1 | — | — | 5 脚本 | 6 |
| 根目录 | — | — | 11 | — | — | 5 | 16 |
| **合计** | **292** | **29** | **32** | **10** | **11** | **25** | **~399** |
