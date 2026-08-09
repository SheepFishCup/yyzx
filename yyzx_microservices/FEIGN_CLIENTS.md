# Feign 调用清单

> 颐养中心微服务 — 服务间调用关系与 OpenFeign 接入计划

---

## 1. 当前状态

**当前版本未引入 OpenFeign**。跨服务数据访问通过以下方式实现：

| 方式 | 说明 |
|------|------|
| **共享数据库直连** | 各模块持有需要的 Mapper，直接查询其他模块"拥有"的表 |
| **RabbitMQ 异步消息** | 通过 `RabbitMQProducerService`（common）发送消息，`yyzx-notification` 统一消费 |

### 为什么暂未引入 Feign

1. **先跑通再优化** — 首次迁移优先保证编译通过、能启动
2. **共享数据库** — 短期内不需要解耦数据库，Feign 引入的收益不大
3. **跨表操作少** — 大部分跨模块引用都是简单的主键查询，Mapper 直连足够高效

---

## 2. 当前跨模块调用分析

### 2.1 共享数据库直连（当前方式）

| 调用方模块 | 被访问的表 | Mapper | 使用场景 |
|-----------|-----------|--------|---------|
| yyzx-customer | bed, beddetails | BedMapper, BedDetailsMapper | 添加客户时创建床位详情、更新床位状态 |
| yyzx-customer | user | UserMapper | 获取管家用户信息 |
| yyzx-bed | customer | CustomerMapper | 床位交换时查询客户信息 |
| yyzx-nursing | customer_nurse_item | CustomerNurseItemMapper | 护理记录扣减库存 |
| yyzx-nursing | outward | OutwardMapper | 查询客户外出记录 |
| yyzx-checkinout | bed | BedMapper | 退住/外出审批后释放床位 |
| yyzx-checkinout | customer | CustomerMapper | 审批后查询/更新客户状态 |
| yyzx-checkinout | user | UserMapper | 获取操作人信息 |
| yyzx-notification | user | UserMapper | AdminNotifyConsumer 查询管理员列表 |
| yyzx-report | customer, bed, backdown, customer_nurse_item | 各 Mapper | 报表数据聚合查询 |
| yyzx-task | backdown, bed, customer | 各 Mapper | 自动审批退住、释放床位 |

### 2.2 RabbitMQ 异步消息（当前方式）

| 调用方 | 消息类型 | 路由键 | 消费者 |
|--------|---------|--------|--------|
| yyzx-auth | 登录通知 | `yyzx.notify` | NotifyConsumer |
| yyzx-auth | 密码重置邮件 | `yyzx.mail` | MailConsumer |
| yyzx-auth | 管理员通知 | `yyzx.admin.notify` | AdminNotifyConsumer |
| yyzx-auth | 操作日志 | `yyzx.log` | LogConsumer |
| yyzx-customer | 客户创建通知 | `yyzx.admin.notify` | AdminNotifyConsumer |
| yyzx-customer | 操作日志 | `yyzx.log` | LogConsumer |
| yyzx-task | 失败邮件重试 | `yyzx.mail` | MailConsumer |

---

## 3. 推荐 Feign 接口设计

> 当数据库需要解耦时，将以下 Mapper 直连改为 Feign 调用。

### 3.1 BedFeignClient（床位服务提供）

```java
// 模块: yyzx-bed（提供方）
// 包: com.cqupt.bed.feign
@FeignClient(name = "yyzx-bed", path = "/bed")
public interface BedFeignClient {

    /** 查询床位是否可用 */
    @GetMapping("/checkAvailable")
    ResultVo<Boolean> checkAvailable(@RequestParam("bedId") Integer bedId);

    /** 更新床位状态 */
    @PutMapping("/updateStatus")
    ResultVo<Void> updateStatus(@RequestParam("bedId") Integer bedId,
                                 @RequestParam("status") Integer status);

    /** 根据 ID 获取床位信息 */
    @GetMapping("/{bedId}")
    ResultVo<Bed> getById(@PathVariable("bedId") Integer bedId);
}
```

### 3.2 CustomerFeignClient（客户服务提供）

```java
// 模块: yyzx-customer（提供方）
@FeignClient(name = "yyzx-customer", path = "/customer")
public interface CustomerFeignClient {

    /** 根据 ID 获取客户信息 */
    @GetMapping("/{customerId}")
    ResultVo<Customer> getById(@PathVariable("customerId") Long customerId);

    /** 更新客户等级 */
    @PutMapping("/{customerId}/level")
    ResultVo<Void> updateLevel(@PathVariable("customerId") Long customerId,
                                @RequestParam("levelId") Integer levelId);
}
```

### 3.3 UserFeignClient（认证服务提供）

```java
// 模块: yyzx-auth（提供方）
@FeignClient(name = "yyzx-auth", path = "/admin")
public interface UserFeignClient {

    /** 根据 ID 获取用户信息 */
    @GetMapping("/user/{userId}")
    ResultVo<User> getById(@PathVariable("userId") Long userId);

    /** 根据角色获取用户列表 */
    @GetMapping("/users/byRole")
    ResultVo<List<User>> listByRole(@RequestParam("roleId") Integer roleId);
}
```

### 3.4 NursingFeignClient（护理服务提供）

```java
// 模块: yyzx-nursing（提供方）
@FeignClient(name = "yyzx-nursing", path = "/nursecontent")
public interface NursingFeignClient {

    /** 获取护理项目详情（含价格） */
    @GetMapping("/{itemId}")
    ResultVo<NurseContent> getById(@PathVariable("itemId") Integer itemId);

    /** 批量获取护理项目 */
    @GetMapping("/batch")
    ResultVo<List<NurseContent>> listByIds(@RequestParam("ids") List<Integer> ids);
}
```

---

## 4. 调用关系矩阵

### 4.1 当前（共享数据库）

```
         调用方
          auth cust bed  nurs cio  meal notf rpt  task
被调方  ┌─────────────────────────────────────────────┐
auth    │  -    M    -    -    -    -    M    -    -   │  M = Mapper 直连
cust    │  -    -    M    -    M    -    -    M    M   │
bed     │  -    M    -    -    M    -    -    M    M   │
nurs    │  -    -    -    -    -    -    -    -    -   │
cio     │  -    -    -    M    -    -    -    -    -   │
meal    │  -    -    -    -    -    -    -    -    -   │
notf    │  -    -    -    -    -    -    -    -    -   │
report  │  -    -    -    -    -    -    -    -    -   │
task    │  -    -    -    -    -    -    -    -    -   │
└─────────────────────────────────────────────────────┘
```

### 4.2 目标（Feign 解耦后）

```
         调用方
          auth cust bed  nurs cio  meal notf rpt  task
被调方  ┌─────────────────────────────────────────────┐
auth    │  -    F    -    -    -    -    F    -    -   │  F = Feign
cust    │  -    -    F    -    F    -    -    -    F   │
bed     │  -    F    -    -    F    -    -    -    F   │
nurs    │  -    F    -    -    -    -    -    -    -   │
cio     │  -    -    -    -    -    -    -    -    -   │
meal    │  -    -    -    -    -    -    -    -    -   │
└─────────────────────────────────────────────────────┘
```

---

## 5. Feign 接入步骤

### 第1步：添加依赖

在需要使用 Feign 的模块的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### 第2步：启用 Feign

在启动类添加 `@EnableFeignClients`：

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt")
public class YyzxCheckinoutApplication {
    // ...
}
```

### 第3步：编写 Feign 接口

按上述推荐接口设计，在被调用方模块新增 `feign/` 包并编写 FeignClient 接口。

建议：
- **被调用方**定义 Feign 接口（如 `yyzx-bed/feign/BedFeignClient.java`）
- **调用方**同样复制一份 Feign 接口（或抽取到 common 模块）

### 第4步：替换 Mapper 调用

将调用方模块中的 Mapper 跨表查询替换为 Feign 调用：

```java
// 改造前（共享数据库）
@Autowired
private BedMapper bedMapper;

Bed bed = bedMapper.selectById(bedId);

// 改造后（Feign）
@Autowired
private BedFeignClient bedFeignClient;

ResultVo<Bed> result = bedFeignClient.getById(bedId);
Bed bed = result.getData();
```

### 第5步：删除重复 Mapper

调用方不再需要的 Mapper 接口和 XML 文件应从模块中移除。

---

## 6. 迁移优先级建议

| 优先级 | 调用关系 | 理由 |
|--------|---------|------|
| 🔴 高 | checkinout → bed | 床位状态修改是写操作，应通过 Feign 保证数据一致性 |
| 🔴 高 | checkinout → customer | 同上，客户状态修改 |
| 🟡 中 | customer → bed | 添加客户时创建床位详情 |
| 🟡 中 | nursing → customer_nurse_item | 护理记录扣减库存，涉及 Redis Lua 与 DB 双写 |
| 🟢 低 | report → 各表 | 报表只读查询，继续用共享数据库也无妨 |
| 🟢 低 | task → bed/customer | 定时任务可直接操作数据库 |
| ⬜ 已解耦 | auth/customer → notification | 已通过 RabbitMQ 异步解耦，无需 Feign |

---

## 7. Feign 注意事项

1. **超时配置**：Feign 默认连接超时 2s、读超时 5s，报表类长时间查询需调大
2. **熔断降级**：建议配合 Sentinel 使用，数据库不可用时降级返回缓存或默认值
3. **事务问题**：Feign 调用不应放在 `@Transactional` 内（分布式事务需 Seata 等方案）
4. **循环依赖**：避免 A 调 B、B 又调 A 的情况
5. **统一异常处理**：Feign 调用失败应转换为 `BusinessException`，上层统一处理
