# 下一步待办清单

> 颐养中心微服务 — 需要手动配置、验证和后续优化的任务清单

---

## 1. 必须手动配置的项

### 1.1 数据库

- [ ] 确保 `yyzx` 数据库已从 `yyzx_backend/yyzx.sql` 初始化
- [ ] 检查 `application.yaml` 中各模块的数据库连接信息（localhost:3306, root/123456）
- [ ] 如数据库密码不同，修改全部 10 个模块的 `application.yaml` 和 `application-docker.yaml`

### 1.2 Redis

- [ ] 确保 Redis 服务已启动（localhost:6379）
- [ ] 修改 `.env` 和 `application-docker.yaml` 中的 `SPRING_REDIS_PASSWORD`（默认 `yourpassword`）
- [ ] 如果使用本地 Redis 无密码，删除各配置文件中的 `password` 行

### 1.3 RabbitMQ

- [ ] 确保 RabbitMQ 已启动（localhost:5672，管理界面 :15672）
- [ ] 首次启动 `yyzx-notification` 后，检查管理界面确认以下队列已自动创建：
  - `yyzx.mail.queue`（含 5 分钟 TTL）
  - `yyzx.notify.queue`
  - `yyzx.log.queue`
  - `yyzx.admin.notify.queue`
  - `yyzx.mail.dlx.queue` 等死信队列

### 1.4 邮件配置

- [ ] 修改 `yyzx-auth` 和 `yyzx-notification` 中的邮件配置：
  - `spring.mail.host` — SMTP 服务器地址
  - `spring.mail.username` — 发件邮箱（当前默认 `2855733759@qq.com`）
  - `spring.mail.password` — SMTP 授权码（不是邮箱密码！）
- [ ] 测试邮件发送功能

### 1.5 钉钉机器人

- [ ] 在 `yyzx-auth` 和 `yyzx-notification` 中配置钉钉 Webhook 地址和签名密钥
- [ ] 测试钉钉群通知功能

### 1.6 微信小程序

- [ ] 在 `yyzx-customer` 中配置微信 AppID、AppSecret、商户号
- [ ] 测试小程序登录接口 `/patient/login`

### 1.7 JWT 密钥

- [ ] 修改各模块中的 `yyzx.jwt.user-secret`（当前默认 `cqupt123456`）
- [ ] **生产环境必须更换为强随机密钥**

### 1.8 Nacos

- [ ] 启动 Nacos Server（Docker 中已包含，或本地启动）
- [ ] 将 `nacos-config/` 目录下的 11 个 YAML 文件逐个导入 Nacos 配置列表
- [ ] 将各模块 `application.yaml` 中的 `spring.cloud.nacos.config.enabled` 从 `false` 改为 `true`
- [ ] 验证 Nacos 服务列表中能看到所有 10 个微服务

---

## 2. 需要验证的功能点

### 2.1 认证流程

- [ ] 验证码获取接口（`GET /admin/generate`）
- [ ] 登录接口（`POST /admin/loginWithCaptcha`）
- [ ] 登录后 JWT token 返回正确
- [ ] 带 token 访问受保护接口（如 `/customer/listKhxxPage`）
- [ ] 不带 token 访问应返回 401
- [ ] 黑名单功能（连续 5 次密码错误应锁定账户）
- [ ] 忘记密码邮件发送
- [ ] 密码重置流程

### 2.2 客户管理

- [ ] 分页查询客户列表（Redis 缓存生效）
- [ ] 添加客户（自动创建 BedDetails、更新床位状态）
- [ ] 编辑客户信息
- [ ] 删除客户（软删除、释放床位）
- [ ] 客户偏好管理
- [ ] 客户护理项目分配与续费

### 2.3 床位管理

- [ ] 床位按房间/状态查询
- [ ] 房间楼层分布图
- [ ] 床位交换功能（事务性 5 步操作）
- [ ] 床位详情查询

### 2.4 护理服务

- [ ] 护理项目与等级管理
- [ ] 护理记录添加（Redis Lua 原子库存扣减）
- [ ] 库存不足时正确拒绝
- [ ] Redis 不可用时降级到数据库乐观锁

### 2.5 出入管理

- [ ] 退住申请与审批（审批通过后释放床位）
- [ ] 外出申请与审批
- [ ] 返回时间登记

### 2.6 膳食服务

- [ ] 菜品列表（Redis 缓存）
- [ ] 菜品图片上传
- [ ] 每周膳食计划管理

### 2.7 报表导出

- [ ] 客户入住统计查询（`GET /report/customerStats`）
- [ ] 财务统计查询（`GET /report/financeStats`）
- [ ] 客户统计 Excel 导出
- [ ] 财务统计 Excel 导出

### 2.8 通知服务

- [ ] 客户创建后管理员收到钉钉通知
- [ ] 密码重置邮件发送成功
- [ ] WebSocket 连接正常
- [ ] 死信队列：邮件发送失败后正确进入 DLX 并在 5 分钟后重试

### 2.9 定时任务

- [ ] 退住自动审批：超过 24 小时未处理的退住申请自动通过
- [ ] 失败邮件重试：每 5 分钟重试
- [ ] Redis 延迟队列处理：每分钟执行
- [ ] 周报生成：每周一 8:00 执行（目前为桩代码）

### 2.10 Gateway

- [x] 所有路由正确转发到对应微服务 ✅ 2026-08-10 验证通过
- [x] 白名单路径无需 token 即可访问 ✅ `/yyzx/admin/generate` 无需 token
- [x] 非白名单路径缺少 token 返回 401 ✅
- [x] `X-User-Id` 头正确传递给下游服务 ✅ 通过 `BaseContext` 获取
- [ ] CORS 跨域配置生效（前端联调时验证）

---

## 3. 已知问题与注意事项

### 3.1 代码层面

| # | 问题 | 状态 | 说明 |
|---|------|------|------|
| 1 | `WebConfig` 中的静态资源路径 | ✅ 已修复 | `FileUploadConstant` 统一路径，`FoodContoller` + `WebConfig` 共用 `yyzx.upload.path` 配置 |
| 2 | `FileUploadUtil.IMAGE_BASE_PATH` 硬编码 | ✅ 已修复 | 改为引用 `FileUploadConstant.IMAGE_BASE_PATH` + `yyzx.upload.path` 可配置 |
| 3 | `BusinessTask.java` 已移除 | ⬜ 已知 | 全部注释的冗余代码，如需恢复从 `yyzx_backend` 复制 |
| 4 | `ReportServiceImpl.exportExcel()` 双重写入 | ✅ 已修复 | 改为独立的 `exportCustomerStatsExcel` 和 `exportFinanceExcel` |
| 5 | `DingTalkRobotService` 是具体类 | ✅ 已修复 | 抽取接口 → `DingTalkRobotService` + 实现类 `DingTalkRobotServiceImpl` |

### 3.2 架构层面

| # | 问题 | 状态 | 说明 |
|---|------|------|------|
| 1 | 共享数据库 | ⬜ 进行中 | Feign 16 个端点全覆盖，task/notification/checkinout 已清理 Mapper |
| 2 | Mapper 跨模块重复 | ✅ 已修复 | 3 轮共清理 34 个冗余文件（task/notification/checkinout/customer/bed/nursing） |
| 3 | 无分布式事务 | ⬜ 待解决 | 需 Seata 或 Saga 模式 |
| 4 | Gateway JWT Redis 依赖 | ✅ 已修复 | 改为纯 JWT 签名无状态认证 |
| 5 | 无链路追踪 | ✅ 已修复 | SkyWalking Agent 接入 gateway/auth/customer |
| 6 | `/internal/**` 端点无认证 | ✅ 已修复 | `InternalAuthInterceptor` + `FeignRequestInterceptor` 共享密钥机制 |

### 3.3 测试层面

| # | 问题 | 状态 | 说明 |
|---|------|------|------|
| 1 | 只有上下文加载测试，无业务逻辑测试 | ⬜ 用户自测 | 需要时为核心 Service 补充单元测试 |
| 2 | 测试需外部 MySQL/Redis | ✅ 已修复 | H2 内存库 + application-test.yaml 排除 Redis/RabbitMQ/Nacos |
| 3 | 无 API 集成测试 | ⬜ 后续 | 后续使用 Testcontainers 或 MockMvc |

---

## 4. 安全加固清单

- [ ] 更换默认 JWT 密钥 `cqupt123456`
- [ ] 更换 Druid 监控面板默认密码 `admin/admin`
- [ ] 更换数据库密码 `123456`
- [ ] 更换 Redis 密码
- [ ] 更换 RabbitMQ 默认账号 `guest/guest`
- [ ] 邮件密码改为环境变量，不写入配置文件
- [ ] 钉钉 Webhook Secret 改为环境变量
- [ ] Nacos 开启认证（`NACOS_AUTH_ENABLE=true`）
- [ ] Gateway 添加请求频率限制（Rate Limiter）
- [ ] 敏感接口添加操作审计日志

---

## 5. 下一步优化建议

### ✅ 已完成（2026-07 架构升级）

1. ✅ **OpenFeign** — 全部 9 个模块 + `@EnableFeignClients`，common 中 7 个 Feign 接口，16 个端点覆盖全部跨模块调用
2. ✅ **Resilience4j FallbackFactory** — 4 种 FeignClient 挂载降级工厂
3. ✅ **Sentinel 流量防卫兵** — Gateway + `listKhxxPage` `@SentinelResource`（QPS=100 + 熔断）
4. ✅ **SkyWalking 链路追踪** — gateway/auth/customer 接入，日志 `[%X{traceId}]` 传播
5. ✅ **无状态 JWT 认证** — Gateway 移除 Redis 依赖
6. ✅ **DingTalkRobotService 接口抽取** — 与其他 Service 架构对齐
9. ✅ **跨模块 Mapper 清理（第2-3轮）** — customer/bed/nursing 共清理 12 个文件，全项目 34 个冗余文件，9 模块中 7 个已干净
10. ✅ **测试配置完善** — H2 内存库 + `application-test.yaml`（10 个模块），排除全部外部依赖，`mvn test` 可离线运行
11. ✅ **SkyWalking Agent 就位** — `scripts/skywalking-agent/skywalking-agent.jar`（apm-agent-9.3.0, 22MB）

### 5.1 短期（1-2 周）

1. 完成上述"必须手动配置的项"全部勾选
2. 完成"需要验证的功能点"全部测试通过
3. 编写核心 Service 的业务逻辑单元测试
4. 配置 CI/CD 流水线（GitHub Actions / Jenkins）
5. 启动 Sentinel Dashboard 并在控制台配置 Gateway QPS 规则
6. 下载 SkyWalking Agent 验证 TraceId 在日志中的传递

### 5.2 中期（1-2 月）

1. 持续将跨服务 Mapper 替换为 Feign 调用（按 FEIGN_CLIENTS.md 优先级）
2. ~~引入 Sentinel~~ ✅ 已完成
3. ~~引入 SkyWalking 实现链路追踪~~ ✅ 已完成（替代原计划的 Sleuth+Zipkin）
4. 拆分数据库（按模块创建独立 Schema，逐步迁移）

### 5.3 长期（3-6 月）

1. 引入 **Seata** 解决分布式事务问题
2. 引入 **Canal + Binlog** 实现数据同步
3. 容器化部署到 **Kubernetes**
4. 引入 **ELK**（Elasticsearch + Logstash + Kibana）统一日志管理
5. 引入 **Prometheus + Grafana** 实现服务监控告警

---

## 6. 常用命令速查

```bash
# 编译整个项目
cd yyzx_microservices
mvn clean install -DskipTests

# 编译单个模块
mvn clean package -pl yyzx-auth -DskipTests

# 运行单个模块测试
mvn test -pl yyzx-auth

# Docker Compose 部署
docker-compose up -d --build
docker-compose logs -f yyzx-gateway
docker-compose down

# 查看端口占用
netstat -ano | findstr 8080

# Nacos 服务发现测试
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=yyzx-auth
```
