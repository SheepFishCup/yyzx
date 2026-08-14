# 分布式事务方案（Seata AT 模式）

> 颐养中心微服务 — 跨服务数据一致性解决方案

---

## 1. 问题背景

微服务拆分为 11 个模块后，以下业务操作跨越了多个服务：

| 业务操作 | 涉及服务 | 数据变更 |
|---------|---------|---------|
| 入住登记 `addCustomer` | customer → bed | customer 插入 + beddetails 插入 + bed 状态更新 |
| 退住审批 `examineBackdown` | checkinout → customer → bed | backdown 审批 + bed 释放 |
| 外出审批 `examineOutward` | checkinout → customer → bed | outward 审批 + bed 状态=外出 |
| 床位交换 `exchangeBed` | bed → customer | beddetails 关闭/新建 + bed×2 + customer 更新 |

**风险场景：** 客户已插入数据库，但 Feign 调用 bed 服务超时 → 客户记录存在但床位未占用 → 数据不一致。

---

## 2. 方案选型

| 方案 | 优点 | 缺点 | 适用 |
|------|------|------|------|
| **Seata AT** ✅ | 低侵入（注解即可）、自动回滚 | 需部署 Seata Server | 本项目（共享数据库） |
| Seata TCC | 性能最好 | 需手写 Try/Confirm/Cancel | 高性能场景 |
| Saga | 长事务友好 | 需手写补偿逻辑 | 长流程 |
| 本地消息表 | 最终一致性 | 开发量大 | 异步场景 |

**选型结论：Seata AT 模式** — 共享数据库场景下 AT 模式利用 undo_log 实现自动回滚，代码侵入最小。

---

## 3. 已实施的改动

### 3.1 依赖（父 POM + 4 个服务）

```xml
<!-- 父 POM 版本管理 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
    <version>${spring-cloud-alibaba.version}</version>
</dependency>
<dependency>
    <groupId>io.seata</groupId>
    <artifactId>seata-spring-boot-starter</artifactId>
    <version>1.5.2</version>
</dependency>
```

**已添加 Seata 依赖的服务：** yyzx-customer、yyzx-bed、yyzx-checkinout、yyzx-task

### 3.2 配置（application.yaml）

```yaml
seata:
  enabled: false              # 本地开发默认关闭；接入 Seata Server 后改为 true
  tx-service-group: yyzx-tx-group
  registry:
    type: nacos
    nacos:
      server-addr: localhost:8848
      group: SEATA_GROUP
      application: seata-server
  config:
    type: nacos
    nacos:
      server-addr: localhost:8848
      group: SEATA_GROUP
  data-source-proxy-mode: AT
```

### 3.3 事务注解（4 个全局事务点）

```java
// CustomerServiceImpl
@GlobalTransactional(name = "customer-add-customer", rollbackFor = Exception.class)
public ResultVo<?> addCustomer(Customer customer) { ... }

@GlobalTransactional(name = "customer-remove-customer", rollbackFor = Exception.class)
public ResultVo<?> removeCustomer(Long id, Integer bedId) { ... }

// BedDetailsServiceImpl
@GlobalTransactional(name = "bed-exchange-bed", rollbackFor = Exception.class)
public ResultVo exchangeBed(ExchangeDTO exchangeDTO) { ... }

// BackdownController / OutwardController
@GlobalTransactional(name = "checkinout-examine-backdown", rollbackFor = Exception.class)
public ResultVo<?> examineBackdown(Backdown backdown) { ... }
```

---

## 4. 部署 Seata Server

### 4.1 下载与配置

```bash
# 下载 Seata Server 1.5.2
# https://github.com/seata/seata/releases/download/v1.5.2/seata-server-1.5.2.tar.gz

# 修改 conf/application.yml 注册到 Nacos
seata:
  registry:
    type: nacos
    nacos:
      server-addr: localhost:8848
      group: SEATA_GROUP
      application: seata-server
  config:
    type: nacos
    nacos:
      server-addr: localhost:8848
      group: SEATA_GROUP
  store:
    mode: db
    db:
      url: jdbc:mysql://localhost:3306/seata
      user: root
      password: 123456
```

### 4.2 初始化 Seata 数据库

```sql
-- 创建 seata 库并执行 seata 官方建表脚本
CREATE DATABASE seata;
-- 执行 seata-server/conf/db_store.sql
```

### 4.3 业务库执行 undo_log 建表

```bash
mysql -u root -p yyzx < scripts/seata-undo-log.sql
```

### 4.4 启动

```bash
bin/seata-server.bat -p 8091
```

### 4.5 开启服务事务

将各服务 `application.yaml` 中 `seata.enabled` 改为 `true`，重启服务。

---

## 5. 事务流程图

```
客户入住 addCustomer（全局事务 XID）
    │
    ├─ 分支1: customer 表 insert（customer 服务）
    │     └─ undo_log 记录反向 SQL
    │
    ├─ Feign → 分支2: beddetails 表 insert（bed 服务）
    │     └─ undo_log 记录反向 SQL
    │
    ├─ Feign → 分支3: bed 表 update 状态=占用（bed 服务）
    │     └─ undo_log 记录反向 SQL
    │
    ├─ ✅ 全部成功 → Seata Server 提交全局事务
    │     └─ 各分支删除 undo_log
    │
    └─ ❌ 任一失败 → Seata Server 回滚全局事务
          └─ 各分支按 undo_log 执行反向 SQL 恢复数据
```

---

## 6. 与现有容错体系的关系

```
                    ┌── 同步链路: @GlobalTransactional (Seata)
请求 → Gateway → 业务方法
                    └── 调用降级: FallbackFactory (Resilience4j)
                              └─ Feign 失败返回 ResultVo.fail("xxx服务暂不可用")
                                     └─ 业务代码抛异常 → Seata 回滚全部数据
```

三层保护：
1. **Resilience4j** — Feign 超时/失败不抛裸异常，返回降级结果
2. **业务校验** — 检查降级结果 `isFlag()`，失败抛 `RuntimeException`
3. **Seata** — 捕获异常后回滚所有分支事务

---

## 7. 注意事项

| 事项 | 说明 |
|------|------|
| 本地开发默认关闭 | `seata.enabled: false`，不影响现有功能 |
| 共享数据库 | 所有分支的 undo_log 都在同一个 yyzx 库中 |
| RabbitMQ 消息 | 事务提交后才发消息（当前代码顺序已满足） |
| Redis 缓存 | 缓存操作不参与事务，事务失败需手动清缓存（`clearCustomerCache()`） |
| 回滚限制 | 仅回滚 DB 数据，Feign 调用产生的副作用（如已发送的通知）无法撤销 |
