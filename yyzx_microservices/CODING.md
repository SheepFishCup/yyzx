# 开发规范

> 颐养中心微服务 — 代码风格、包结构、Git 提交规范

---

## 1. 包结构规范

### 1.1 公共模块（yyzx-common）

```
com.cqupt
├── annotation/      # 自定义注解
├── constant/        # 常量类（每种中间件一个常量文件）
├── context/         # 线程上下文
├── dto/             # 数据传输对象
├── exception/       # 自定义异常
├── handler/         # 全局处理器（异常处理、自动填充）
├── interceptor/     # 拦截器
├── pojo/            # 实体类（与数据库表一一对应）
├── properties/      # @ConfigurationProperties 配置类
├── rabbit/          # RabbitMQ 公共生产者
├── utils/           # 工具类
├── valid/groups/    # 校验分组
├── validator/       # 自定义校验器
└── vo/              # 视图对象（API 返回的复杂对象）
```

### 1.2 业务模块（以 yyzx-auth 为例）

```
com.cqupt.auth
├── YyzxAuthApplication.java    # 启动类
├── config/                     # 本模块的 Spring 配置
│   ├── RedisConfig.java
│   ├── MybatisPlusConfig.java
│   └── ...
├── controller/                 # REST 控制器
├── mapper/                     # MyBatis Mapper 接口
├── service/                    # 服务接口
│   └── impl/                   # 服务实现
└── ...                         # 如有其他特有包
```

### 1.3 命名规则

| 元素 | 规则 | 示例 |
|------|------|------|
| 模块名 | `yyzx-{领域}` | `yyzx-auth`, `yyzx-customer` |
| 基础包名 | `com.cqupt.{领域}` | `com.cqupt.auth` |
| 启动类 | `Yyzx{领域}Application` | `YyzxAuthApplication` |
| 实体类 | 与表名对应（驼峰） | `Customer`, `NurseContent` |
| Mapper | `{实体}Mapper` | `CustomerMapper` |
| Service 接口 | `{实体}Service` 或业务名 | `CustomerService`, `ReportService` |
| Service 实现 | `{接口名}Impl` | `CustomerServiceImpl` |
| Controller | `{实体/业务}Controller` | `CustomerController` |
| DTO | `{用途}DTO` | `LoginWithCodeDTO` |
| VO | `{用途}Vo` | `KhxxCustomerVo` |
| 常量类 | `{中间件/业务}Constant` | `RedisConstant`, `RabbitMQConstant` |
| 配置类 | `{中间件/功能}Config` | `RedisConfig`, `QuartzConfig` |
| 工具类 | `{功能}Util` 或 `{功能}Utils` | `JwtUtil`, `RedisLuaUtils` |
| 异常类 | `{原因}Exception` | `AccountLockedException` |

---

## 2. 代码风格规范

### 2.1 Java 代码

遵循 **阿里巴巴 Java 开发手册**，关键要点：

```java
// ✅ 正确：使用 Lombok 简化代码
@Data
@TableName("customer")
public class Customer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String customerName;
}

// ✅ 正确：统一响应格式
@GetMapping("/list")
public ResultVo<Page<CustomerVo>> list(QueryDTO dto) {
    return ResultVo.ok(customerService.pageList(dto));
}

// ✅ 正确：异常处理
if (customer == null) {
    throw new BusinessException("客户不存在");
}

// ✅ 正确：日志规范
log.info("用户 {} 登录成功", username);
log.error("添加客户失败，客户姓名={}", dto.getCustomerName(), e);

// ❌ 错误：使用 System.out.println
System.out.println("debug info");  // 禁止

// ❌ 错误：吞掉异常
try {
    // ...
} catch (Exception e) {
    // 空 catch 块
}
```

### 2.2 注释规范

```java
/**
 * 客户服务实现
 * <p>负责客户 CRUD 及关联操作（床位分配、护理项目绑定）</p>
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    /**
     * 添加客户
     * <p>事务操作：创建客户 → 创建 BedDetails → 更新床位状态 → 发送通知</p>
     *
     * @param customer 客户信息
     * @return 创建成功的客户对象
     * @throws BusinessException 床位不可用或客户信息不合法
     */
    @Transactional(rollbackFor = Exception.class)
    public Customer addCustomer(Customer customer) {
        // ...
    }
}
```

### 2.3 依赖注入

```java
// ✅ 推荐：构造器注入（便于测试）
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserServiceImpl(UserMapper userMapper,
                           RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }
}

// ⚠️ 可接受：字段注入（本项目历史代码多采用此方式，不强制修改）
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
}
```

---

## 3. 配置规范

### 3.1 多环境配置

```
resources/
├── application.yaml           # 本地开发（硬编码默认值）
└── application-docker.yaml    # Docker 部署（使用环境变量）
```

**要求：**
- `application.yaml` 中写死本地开发默认值（可直接运行）
- `application-docker.yaml` 中敏感信息使用 `${ENV_VAR:default}` 占位符
- 不要在任何配置文件中提交真实密码/密钥

### 3.2 端口分配

| 服务 | 端口 |
|------|------|
| yyzx-gateway | 8080 |
| yyzx-auth | 8081 |
| yyzx-customer | 8082 |
| yyzx-bed | 8083 |
| yyzx-nursing | 8084 |
| yyzx-checkinout | 8085 |
| yyzx-meal | 8086 |
| yyzx-notification | 8087 |
| yyzx-report | 8088 |
| yyzx-task | 8089 |

> 端口范围 8080-8089，新增服务请从 8090 开始。

### 3.3 公共配置复用

所有服务的以下配置应从 `yyzx-common` 或 Nacos 共享配置中继承：
- 数据源配置
- Redis 配置
- RabbitMQ 配置
- MyBatis-Plus 配置
- JWT 配置

---

## 4. 数据库规范

### 4.1 共享数据库访问规则

> 当前采用共享数据库模式。每个模块可访问自己"拥有"的表以及必要的只读跨表。

| 模块 | 拥有的表 | 跨表读取 |
|------|---------|---------|
| yyzx-auth | user, role, menu, role_menu | — |
| yyzx-customer | customer, customer_preference, customer_nurse_item, patient | bed, beddetails |
| yyzx-bed | bed, beddetails, room | customer |
| yyzx-nursing | nursecontent, nurselevel, nurselevelitem, nurserecord | customer_nurse_item, outward |
| yyzx-checkinout | backdown, outward | bed, customer |
| yyzx-meal | food, meal | — |
| yyzx-notification | failed_mail_record | user |
| yyzx-report | — | customer, bed, backdown, customer_nurse_item |
| yyzx-task | — | backdown, bed, customer, failed_mail_record |

### 4.2 Mapper XML 规范

```xml
<!-- 命名空间必须与 Mapper 接口完整路径一致 -->
<mapper namespace="com.cqupt.auth.mapper.UserMapper">

    <!-- resultMap 定义在对应的 VO 查询 Mapper 中 -->
    <resultMap id="UserVoMap" type="com.cqupt.vo.UserVo">
        <id column="id" property="id"/>
        <!-- ... -->
    </resultMap>

    <!-- SQL 语句使用 MyBatis-Plus 的 QueryWrapper 优先，复杂查询才写 XML -->
</mapper>
```

---

## 5. Git 提交规范

### 5.1 分支策略

```
main                  # 主分支（生产代码）
├── develop           # 开发分支
│   ├── feature/*     # 功能分支（如 feature/add-wechat-pay）
│   ├── fix/*         # 修复分支（如 fix/login-captcha-error）
│   └── refactor/*    # 重构分支（如 refactor/extract-common）
└── release/*         # 发布分支
```

### 5.2 Commit Message 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type 类型

| Type | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `refactor` | 重构（不改变功能） |
| `docs` | 文档更新 |
| `style` | 代码格式（不影响逻辑） |
| `test` | 添加/修改测试 |
| `chore` | 构建/工具/依赖更新 |
| `perf` | 性能优化 |

#### Scope 范围

使用模块名或功能领域：
`auth`, `customer`, `bed`, `nursing`, `checkinout`, `meal`, `notification`, `report`, `task`, `common`, `gateway`

#### 示例

```bash
feat(auth): 添加滑动验证码支持

- 替换 Kaptcha 为腾讯滑动验证码
- 增加验证码过期自动清理定时任务

Closes #42

fix(customer): 修复客户查询缓存击穿问题

- Redis 不可用时降级为直接查数据库
- 添加本地 ConcurrentHashMap 二级缓存

fix(report): 重写报表模块 9 个 bug

- 修复 retreat_date 字段不存在导致 SQL 崩溃
- 修复 nursingLevel==1 错误计入 selfCare
- 修复 exportExcel 双重写入 response
- 修复模板匹配参数写反
- 财务数据改为基于真实表计算

refactor(common): 抽取 RabbitMQ 常量为公共常量类

新增 RabbitMQConstant 避免各模块重复定义队列名

docs: 添加架构文档和启动说明

Co-Authored-By: Claude Code <noreply@anthropic.com>
```

### 5.3 提交前检查

```bash
# 1. 本地编译通过
mvn clean compile

# 2. 测试通过
mvn test

# 3. 检查无 TODO/FIXME 残留
grep -r "TODO\|FIXME" --include="*.java" .

# 4. 检查无 System.out.println
grep -r "System.out.println" --include="*.java" .
```

---

## 6. 常见开发场景

### 6.1 添加新的 REST 端点

```java
@RestController
@RequestMapping("/customer")
@Api(tags = "客户管理")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @ApiOperation("分页查询客户列表")
    @GetMapping("/listKhxxPage")
    public ResultVo<Page<KhxxCustomerVo>> list(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("客户名称") @RequestParam(required = false) String customerName) {

        Page<KhxxCustomerVo> page = customerService.KhxxFindCustomer(
                current, pageSize, customerName);
        return ResultVo.ok(page);
    }
}
```

### 6.2 添加新的 RabbitMQ 消息

1. 在 `RabbitMQConstant` 中添加队列/路由键常量
2. 在 `yyzx-notification` 的 `RabbitMQConfig` 中声明队列和绑定
3. 在 `yyzx-notification` 中编写 Consumer
4. 业务模块通过 `RabbitMQProducerService` 发送消息

### 6.3 添加新的定时任务

1. 在 `yyzx-task/job/` 中创建 Job 类（继承 `QuartzJobBean`）
2. 在 `QuartzConfig` 中注册 JobDetail 和 Trigger
3. 如需访问数据库，在 `yyzx-task/mapper/` 中添加对应 Mapper

### 6.4 跨模块复用代码

- 如果 2 个以上模块使用 → 放入 `yyzx-common`
- 如果仅 1 个模块使用 → 保留在该模块内
- Mapper 允许跨模块重复（共享数据库模式下的必要代价）
