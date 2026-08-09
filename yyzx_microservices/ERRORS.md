# 常见错误与解决方案

> 颐养中心微服务 — 编译、启动、运行时常见错误排查指南

---

## 1. 编译阶段

### 1.1 Maven 依赖找不到

**现象：**
```
[ERROR] Failed to execute goal on project yyzx-auth:
Could not resolve dependencies for project com.cqupt:yyzx-auth:jar:1.0.0-SNAPSHOT:
Could not find artifact com.cqupt:yyzx-common:jar:1.0.0-SNAPSHOT
```

**原因：** `yyzx-common` 模块未安装到本地 Maven 仓库

**解决：**
```bash
# 先安装 common 模块
cd yyzx_microservices
mvn clean install -pl yyzx-common -DskipTests

# 再编译目标模块
mvn clean compile -pl yyzx-auth
```

---

### 1.2 Lombok 注解不生效

**现象：**
```
[ERROR] cannot find symbol
  symbol:   method getUsername()
  location: variable user of type User
```

**原因：** IDE 未启用 Lombok 注解处理器，或 Lombok 版本不兼容

**解决：**
- **IntelliJ IDEA**：`Settings → Build → Compiler → Annotation Processors → Enable annotation processing`
- **IntelliJ IDEA**：安装 Lombok 插件（`File → Settings → Plugins → 搜索 Lombok`）
- **Eclipse**：运行 Lombok jar 安装（`java -jar lombok.jar`）
- **Maven**：确认 `pom.xml` 中 Lombok 版本与 IDE 插件版本兼容

---

### 1.3 Java 版本不匹配

**现象：**
```
[ERROR] class file has wrong version 55.0, should be 52.0
```

**原因：** 编译 class 的 Java 版本（55 = Java 11）高于运行环境（52 = Java 8）

**解决：**
```bash
# 检查当前 Java 版本
java -version

# 确认 pom.xml 中版本为 1.8
# <java.version>1.8</java.version>
# <maven.compiler.source>1.8</maven.compiler.source>
# <maven.compiler.target>1.8</maven.compiler.target>

# 如使用 IDE，检查 Project SDK 设为 JDK 1.8
```

---

### 1.4 spring-boot-maven-plugin 找不到

**现象：**
```
[ERROR] No plugin found for prefix 'spring-boot' in the current project
```

**原因：** 父 POM 的 `pluginManagement` 未正确定义插件版本

**解决：**
```bash
# 在父 pom.xml 确认有以下配置：
# <pluginManagement>
#   <plugins>
#     <plugin>
#       <groupId>org.springframework.boot</groupId>
#       <artifactId>spring-boot-maven-plugin</artifactId>
#       <version>${spring-boot.version}</version>
#     </plugin>
#   </plugins>
# </pluginManagement>
```

---

### 1.5 包名拼写错误

**现象：**
```
[ERROR] package com.cqupt.annotaion does not exist
[ERROR] cannot find symbol: class File
```

**原因：** 原单体项目中 `@File` / `@MultiFieldAssociationCheck` 注解的 import 有拼写错误（`annotaion` → `annotation`）

**解决：** 此问题已在代码审查中修复。如仍出现，检查：
```java
// 错误：
import com.cqupt.annotaion.File;

// 正确：
import com.cqupt.annotation.File;
```

---

## 2. 启动阶段

### 2.1 端口被占用

**现象：**
```
***************************
APPLICATION FAILED TO START
***************************
Description:
Web server failed to start. Port 8081 was already in use.
```

**原因：** 目标端口已被其他进程占用

**解决：**
```bash
# Windows 查看端口占用
netstat -ano | findstr 8081

# 终止占用进程（替换 PID）
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8081
kill -9 <PID>

# 或修改 application.yaml 端口
server:
  port: 8082  # 改为未占用的端口
```

---

### 2.2 Nacos 连接失败

**现象：**
```
com.alibaba.nacos.api.exception.NacosException:
failed to req API: /nacos/v1/ns/instance after all servers
```

**原因：** Nacos Server 未启动，或地址配置错误

**解决：**
```bash
# 1. 确认 Nacos 已启动
docker ps | grep nacos

# 2. 确认 Nacos 地址正确
# application.yaml:
# spring.cloud.nacos.discovery.server-addr: localhost:8848

# 3. 如果本地开发不需要 Nacos，在 application.yaml 中禁用
spring:
  cloud:
    nacos:
      discovery:
        enabled: false
      config:
        enabled: false

# 或在测试/启动时添加参数
mvn spring-boot:run -pl yyzx-auth \
  -Dspring.cloud.nacos.discovery.enabled=false \
  -Dspring.cloud.nacos.config.enabled=false
```

---

### 2.3 数据库连接失败

**现象：**
```
com.mysql.cj.jdbc.exceptions.CommunicationsException:
Communications link failure
```

**原因：** MySQL 未启动、地址/端口错误、密码错误、数据库不存在

**解决：**
```bash
# 1. 确认 MySQL 已启动
docker ps | grep mysql
mysql -u root -p -h localhost

# 2. 确认数据库已创建
mysql -u root -p -e "SHOW DATABASES;" | grep yyzx

# 3. 如未初始化，执行 SQL
mysql -u root -p < ../yyzx_backend/yyzx.sql

# 4. 检查 application.yaml 中连接信息
# spring.datasource.druid.url: jdbc:mysql://localhost:3306/yyzx
# spring.datasource.druid.username: root
# spring.datasource.druid.password: 123456
```

---

### 2.4 Redis 连接失败

**现象：**
```
org.springframework.data.redis.RedisConnectionFailureException:
Unable to connect to Redis
```

**原因：** Redis 未启动，或密码配置错误

**解决：**
```bash
# 1. 确认 Redis 已启动
docker ps | grep redis
redis-cli ping

# 2. 如果有密码，确认配置一致
redis-cli -a yourpassword ping

# 3. 检查 application.yaml
# spring.redis.host: localhost
# spring.redis.port: 6379
# spring.redis.password:    # 无密码则留空或删除此行

# 4. 无 Redis 也可启动（缓存功能降级），但登录等操作会失败
```

---

### 2.5 RabbitMQ 连接失败

**现象：**
```
org.springframework.amqp.AmqpConnectException:
java.net.ConnectException: Connection refused
```

**原因：** RabbitMQ 未启动

**解决：**
```bash
# 1. 确认 RabbitMQ 已启动
docker ps | grep rabbitmq

# 2. 访问管理界面确认
# http://localhost:15672 (guest/guest)

# 3. 如果开发中用不到 RabbitMQ，排除自动配置
# 在启动类添加：
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class
})
```

---

### 2.6 Bean 注入失败

**现象：**
```
Field xxxMapper in com.cqupt.xxx.service.impl.XxxServiceImpl
required a bean of type 'com.cqupt.xxx.mapper.XxxMapper' that could not be found.
```

**原因：** `@MapperScan` 未扫描到 Mapper 包，或 Mapper 包路径与声明不一致

**解决：**
```java
// 检查启动类的 @MapperScan
@MapperScan("com.cqupt.auth.mapper")   // 必须与 Mapper 接口包路径一致

// 如果是跨模块引用，确认 Mapper 接口文件确实存在于该模块
// 例如：yyzx-customer 中使用了 UserMapper，
// 必须确认 src/main/java/com/cqupt/customer/mapper/UserMapper.java 存在
```

---

## 3. 运行时

### 3.1 Gateway 路由 404

**现象：**
```
GET http://localhost:8080/customer/listKhxxPage → 404 Not Found
```

**排查步骤：**
```bash
# 1. 确认目标服务已注册到 Nacos
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=yyzx-customer

# 2. 确认路由配置正确（application.yaml 中 spring.cloud.gateway.routes）

# 3. 确认请求路径与路由 predicates 匹配
# 路由: Path=/customer/** 
# 请求: /customer/listKhxxPage  ✅
# 请求: /yyzx/customer/listKhxxPage  ❌ (不带 context-path)

# 4. 开启 Gateway 调试日志
logging:
  level:
    org.springframework.cloud.gateway: TRACE
```

---

### 3.2 JWT 返回 401 未授权

**现象：**
```json
{"flag":false,"message":"token 已过期，请重新登录","data":null}
```

**排查步骤：**
```bash
# 1. 确认 token 在请求 Header 中
curl -H "token: eyJhbGciOiJIUzUxMiJ9..." http://localhost:8080/customer/listKhxxPage

# 2. 检查 token 是否过期（默认 2 小时）

# 3. 检查 Redis 中 token 是否存在
redis-cli
> GET user:token:1

# 4. 确认路径不在白名单中（白名单路径跳过认证）
```

---

### 3.3 跨域 CORS 错误

**现象：**
```
Access to XMLHttpRequest at 'http://localhost:8080/admin/generate'
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**解决：**
```bash
# 1. 确认 GatewayCorsConfig 已正确配置
# 2. 如果前端直连服务（不经过网关），每个服务的 WebConfig 也有 CORS 配置
# 3. 确保前端请求携带 withCredentials（如果需要 cookie）
```

---

### 3.4 MyBatis Mapper XML 找不到

**现象：**
```
org.apache.ibatis.binding.BindingException:
Invalid bound statement (not found): com.cqupt.auth.mapper.UserMapper.selectById
```

**原因：** Mapper XML 的 namespace 与接口路径不一致

**解决：**
```bash
# 1. 检查 XML 命名空间
# UserMapper.xml:
# <mapper namespace="com.cqupt.auth.mapper.UserMapper">

# 2. 检查 application.yaml
# mybatis-plus.mapper-locations: classpath:/mapper/*.xml

# 3. 确认 XML 文件确实在 resources/mapper/ 目录下
ls src/main/resources/mapper/
```

---

### 3.5 文件上传失败

**现象：**
```
java.io.FileNotFoundException: ./static/images/food_xxx.jpg
```

**原因：** 上传路径不存在或无写入权限

**解决：**
```bash
# FileUploadUtil 使用 user.dir + /static/images/ 作为上传路径
# Docker 中需要挂载卷或改用 OSS
mkdir -p static/images
```

---

### 3.6 定时任务不执行

**现象：** Quartz 定时任务没有按时触发

**排查步骤：**
```bash
# 1. 确认 QuartzConfig 中 JobDetail 和 Trigger 已注册
# 2. 确认 Scheduler Bean 已创建且 autoStartup = true
# 3. 查看启动日志：
#    "Scheduler class: 'org.quartz.core.QuartzScheduler'"
# 4. 确认 cron 表达式正确
#    "0 0 * * * ?" = 每小时整点
#    "0 */5 * * * ?" = 每 5 分钟
#    "0 0 8 ? * MON" = 每周一早 8 点
```

---

### 3.7 RabbitMQ 消息发送了但未被消费

**现象：** RabbitMQ 管理界面中队列积压消息

**排查步骤：**
```bash
# 1. 确认 yyzx-notification 服务已启动
# 2. 确认消费者未被异常关闭
# 3. 确认 acknowledge-mode 设置正确（当前为 manual）
# 4. 检查死信队列是否有消息（邮件队列 5 分钟 TTL 后进入 DLX）
```

---

## 4. Docker 部署问题

### 4.1 容器启动后立即退出

**现象：**
```
yyzx-auth exited with code 1
```

**排查：**
```bash
# 查看容器日志
docker logs yyzx-auth

# 常见原因：数据库/Redis 未就绪
# 解决：增加 depends_on + healthcheck 等待时间
```

---

### 4.2 构建时 COPY 失败

**现象：**
```
COPY failed: file not found in build context
```

**原因：** Docker 构建上下文路径错误

**解决：**
```yaml
# docker-compose.yml 中：
build:
  context: .          # 必须是 yyzx_microservices 目录
  dockerfile: yyzx-auth/Dockerfile

# 确保 Dockerfile 中的 COPY 路径相对于 context
# COPY pom.xml /build/            ← 指 yyzx_microservices/pom.xml
# COPY yyzx-common/src /build/yyzx-common/src
```

---

## 5. SkyWalking 相关问题

### 5.1 .bat 脚本中文乱码 / 命令被截断

**现象：**
```
PS> .\scripts\start-gateway.bat
'teway' 不是内部或外部命令
'癸級' 不是内部或外部命令
'walking-agent' 不是内部或外部命令
```

**原因：** `.bat` 文件默认使用系统 GBK 编码，UTF-8 编码的中文字符会被 PowerShell/CMD 解析为乱码，导致每行被截断成多个"命令"。

**解决：**
1. `.bat` 文件全部使用**英文注释和无中文 echo**（推荐）
2. 或者在文件开头添加 `chcp 65001 >nul` 切换到 UTF-8 代码页
3. PowerShell 下建议直接使用 `mvn spring-boot:run` + 手动加 JVM 参数

### 5.2 SkyWalking Agent 在二进制包中不存在

**现象：** 下载的 `apache-skywalking-apm-X.X.X-bin.tar.gz` 解压后没有 `agent/` 目录，找不到 `skywalking-agent.jar`

**原因：** SkyWalking 10.x 起将 Java Agent 从二进制 OAP 包中移除了，Agent 作为独立组件发布

**解决：**
```bash
# 方式1: 从 Maven Central 下载独立 Agent（推荐）
curl -L -o scripts/skywalking-agent/skywalking-agent.jar \
  https://repo1.maven.org/maven2/org/apache/skywalking/apm-agent/9.3.0/apm-agent-9.3.0.jar

# 方式2: 使用 SkyWalking 8.9.1（最后一个含 Agent 的完整包）
curl -L -o skywalking.tgz \
  https://archive.apache.org/dist/skywalking/8.9.1/apache-skywalking-apm-8.9.1.tar.gz
# 注意：8.9.1 官网也已移除 Agent，推荐方式1
```

### 5.3 `-src.tgz` 源码包无法直接使用

**现象：** 下载了 `apache-skywalking-apm-X.X.X-src.tgz`，解压后没有 `bin/` 目录

**原因：** 文件名含 `-src` 的是源代码包，需要用 Maven 编译后才能运行

**解决：** 下载 `-bin.tar.gz` 二进制包（不含 `-src` 后缀）

### 5.4 Agent 版本与 OAP 版本兼容性

| Agent 版本 | OAP 版本 | Java 要求 | 兼容 |
|-----------|---------|----------|------|
| 9.3.0 (apm-agent) | 10.4.0 | Java 8 | ⚠️ 跨大版本，协议可能不兼容 |
| 9.3.0 (apm-agent) | 9.x (archive) | Java 8 | ✅ 同大版本 |

> 建议：OAP Server 也用 9.x 版本以保证与 Agent 9.3.0 的协议兼容。

### 5.5 Sentinel Dashboard 端口与 Gateway 冲突

**现象：** Sentinel Dashboard 和 Gateway 都默认使用 8080 端口

**解决：**
```bash
# Sentinel Dashboard 改用 8090
java -jar sentinel-dashboard-1.8.6.jar --server.port=8090
```
同时检查 `application.yaml` 中 `spring.cloud.sentinel.transport.dashboard` 也改为 `localhost:8090`

---

## 6. Windows 中文路径问题

### 6.1 curl 下载时中文路径报错

**现象：**
```
curl: (23) Failure writing output to destination
```

**原因：** Git Bash 的 curl 对包含中文的 Windows 路径处理有问题

**解决：**
```bash
# 先 cd 到目标目录，再用相对路径
cd "D:/path/to/target"
curl -L -o file.jar "https://..."
# 或使用短路径
```

### 6.2 `ResultVo.ok()` 无参调用编译失败

**现象：** `ResultVo` 类没有无参的 `ok()` 方法

**解决：** 使用 `ResultVo.ok("操作成功")` 传入消息参数

### 6.3 `List.of()` 超过 10 个参数编译失败

**现象：**
```
找不到符号: 方法 of(String,String,...,String) 位置: 接口 java.util.List
```

**原因：** Java 8 的 `List.of()` 最多支持 10 个元素。`AuthProperties.java` 用了 15 个参数

**解决：** 改为 `Arrays.asList()`（无参数数量限制）

### 6.4 YAML 重复 `spring:` key 导致启动崩溃

**现象：**
```
DuplicateKeyException: found duplicate key spring
```

**原因：** Sentinel 配置单独写了一个 `spring:` 块，与顶部的 `spring:` 重复

**解决：** 将 Sentinel 的 `cloud.sentinel.*` 合并到主 `spring.cloud` 块中（与 nacos、gateway 同级）

### 6.5 `sentinelGatewayFilter` Bean 重复定义

**现象：**
```
A bean with that name has already been defined in class path resource
[SentinelSCGAutoConfiguration.class] and overriding is disabled.
```

**原因：** `SentinelGatewayConfig.java` 手动声明了 `sentinelGatewayFilter` Bean，但 `spring-cloud-alibaba-sentinel-gateway` 的自动配置已经提供了同名 Bean。Spring Boot 2.7.x 默认不允许 Bean 覆盖。

**解决：** 从 `SentinelGatewayConfig` 中删除手动声明的 `sentinelGatewayFilter()` 方法，只保留 `@PostConstruct` 中的限流降级回调配置。

### 6.6 端口 8080 被占用（残留进程）

**现象：**
```
Web server failed to start. Port 8080 was already in use.
```

**原因：** 前一次 `mvn spring-boot:run` 的 Java 进程没有被 Ctrl+C 完全终止

**解决：**
```powershell
netstat -ano | findstr :8080           # 找到 PID
taskkill /PID <PID> /F                 # Windows 杀进程
# Linux: lsof -i :8080 && kill -9 <PID>
```

### 6.7 SkyWalking Agent 下载历程

| 尝试 | 地址 | 结果 |
|------|------|------|
| Apache 9.7.0 二进制包 | `dlcdn.apache.org/skywalking/9.7.0/apache-skywalking-apm-9.7.0.tar.gz` | 404 |
| Apache 8.9.1 归档 | `archive.apache.org/dist/skywalking/8.9.1/` | 有包但无 `agent/` 目录 |
| Maven Central `java-agent` | `org/apache/skywalking/java-agent/9.3.0/` | 404 |
| Maven Central `apm-agent` | `org/apache/skywalking/apm-agent/9.3.0/` | ✅ 22MB jar 成功下载 |
| Apache 10.4.0 二进制包 | `dlcdn.apache.org/skywalking/10.4.0/` | ✅ OAP Server 148MB |

> **结论：** SkyWalking 10.x 从二进制包移除了 Java Agent。Agent 需从 Maven Central 的 `apm-agent` 单独下载。本项目使用 `apm-agent-9.3.0.jar`（兼容 Java 8）。

---

## 7. Java 文件结构 / 编译错误

### 7.1 public 类必须在独立文件中声明

**现象：**
```
类 BedDetailsFeignClient 是公共的，应在名为 BedDetailsFeignClient.java 的文件中声明
类 BedDetailsFeignClientFallbackFactory 是公共的，应在名为 BedDetailsFeignClientFallbackFactory.java 的文件中声明
```

**原因：** Java 规定一个 `.java` 文件只能有一个 `public` 类。`BedDetailsFeignClient` 和 `BedDetailsFeignClientFallbackFactory` 原本分别放在 `BedFeignClient.java` 和 `BedFeignClientFallbackFactory.java` 内部，后来被改为 `public`（供跨包注入），但忘记提取到独立文件

**解决：** 各提取为独立文件：
- `BedDetailsFeignClient.java` ← 从 `BedFeignClient.java` 提取
- `BedDetailsFeignClientFallbackFactory.java` ← 从 `BedFeignClientFallbackFactory.java` 提取

### 7.2 `List.of()` 超过 10 个参数（Java 8 限制）

已在 §6.3 记录。

### 7.3 `yyzx-common` 未安装导致依赖解析失败

**现象：**
```
Could not find artifact com.cqupt:yyzx-common:jar:1.0.0-SNAPSHOT
```

**原因：** Gateway 通过父 POM reactor 构建，自动找到 common 模块。但其他服务（auth 等）独立 `mvn spring-boot:run` 时，Maven 从本地仓库查找 `yyzx-common`，找不到就报错

**解决：**
```bash
mvn clean install -pl yyzx-common -DskipTests  # 先安装到本地仓库
mvn spring-boot:run -pl yyzx-auth               # 再启动其他服务
```

### 7.4 `.bat` 脚本中文编码乱码

已在 §5.1 记录。

### 7.5 Sentinel Gateway Bean 重复定义

已在 §6.5 记录。

### 7.6 YAML 重复 key

已在 §6.4 记录。

### 7.7 启动时端口被残留进程占用

已在 §6.6 记录。

### 7.8 父 POM 未安装导致依赖解析失败

**现象：**
```
Could not find artifact com.cqupt:yyzx-microservices:pom:1.0.0-SNAPSHOT
```

**原因：** `mvn clean install -pl yyzx-common` 只安装了 common，但 common 的 parent 是 `yyzx-microservices`。Maven 解析依赖时会先找 parent POM，本地仓库没有就报错

**解决：**
```bash
mvn clean install -N -DskipTests          # 先装父 POM（-N = non-recursive）
mvn clean install -pl yyzx-common -DskipTests  # 再装 common
```

### 7.9 FeignClient 同服务名导致 Bean 名冲突

**现象：**
```
The bean 'yyzx-bed.FeignClientSpecification' could not be registered.
A bean with that name has already been defined and overriding is disabled.
```

**原因：** `BedFeignClient` 和 `BedDetailsFeignClient` 的 `@FeignClient(name = "yyzx-bed")` 相同，Spring Cloud OpenFeign 为每个服务名生成唯一的 `FeignClientSpecification` Bean，同名冲突

**解决：** 给所有 `@FeignClient` 加唯一的 `contextId`：
```java
// BedFeignClient
@FeignClient(name = "yyzx-bed", contextId = "bedFeignClient", ...)

// BedDetailsFeignClient
@FeignClient(name = "yyzx-bed", contextId = "bedDetailsFeignClient", ...)

// BackdownFeignClient
@FeignClient(name = "yyzx-checkinout", contextId = "backdownFeignClient", ...)
```

### 7.10 `@FeignClient` 未加 `contextId` 的完整清单

项目中所有 Feign 接口已统一添加 `contextId`：

| 接口 | name | contextId |
|------|------|-----------|
| `BedFeignClient` | yyzx-bed | bedFeignClient |
| `BedDetailsFeignClient` | yyzx-bed | bedDetailsFeignClient |
| `CustomerFeignClient` | yyzx-customer | customerFeignClient |
| `UserFeignClient` | yyzx-auth | userFeignClient |
| `BackdownFeignClient` | yyzx-checkinout | backdownFeignClient |

### 7.11 `throws Exception` 缺失（多模块）

**现象：** 多个 Controller 编译报 `未报告的异常错误 java.lang.Exception`

**原因：** 重写 Controller 时移除了 `throws Exception`，但底层 Service 接口仍声明了 `throws Exception`

**影响模块：** CustomerController、BackdownController、OutwardController、CheckinoutInternalController

**解决：** 对应方法签名加回 `throws Exception`

### 7.12 POI 3.16 `CellType` 类型不兼容

**现象：** `无法比较的类型: int 和 org.apache.poi.ss.usermodel.CellType`

**原因：** POI 3.16 中 `getCellType()` 返回 `int`，常量是 `Cell.CELL_TYPE_STRING`；POI 4.x+ 才改为 `CellType` 枚举

**解决：** `nameCell.getCellType() != CellType.STRING` → `nameCell.getCellType() != Cell.CELL_TYPE_STRING`

### 7.13 Gateway 路由缺少 `/yyzx` 前缀导致 404

**现象：** `curl http://localhost:8080/admin/generate` → 503 或 404

**原因：** 所有服务配置了 `context-path: /yyzx`，但 Gateway 路由的 `Path=` 没有 `/yyzx` 前缀

**解决：** 所有路由 `Path=/xxx/**` → `Path=/yyzx/xxx/**`

### 7.14 JWT 白名单缺少 `/yyzx` 前缀导致强制认证

**现象：** `curl http://localhost:8080/yyzx/admin/generate` → `"未提供认证 token"`

**原因：** 路由加上 `/yyzx` 前缀后，请求路径变为 `/yyzx/admin/generate`，但 JWT 白名单仍为 `/admin/generate`（不匹配）

**解决：** `AuthProperties.java` 白名单同时添加带 `/yyzx` 前缀的路径；YAML 中删除白名单配置，统一由 Java 代码维护

### 7.15 Redisson 强制依赖导致 notification 启动失败

**现象：**
```
Field redissonClient in com.cqupt.utils.HybridBlacklistUtils required a bean of type 'org.redisson.api.RedissonClient' that could not be found.
```

**原因：** `HybridBlacklistUtils`（common）通过 `@Autowired` 强制注入 `RedissonClient`。notification 服务不使用 Redisson（无分布式锁/布隆过滤器需求），但 common 中的类要求必须有这个 Bean

**解决：**
1. `HybridBlacklistUtils` 中 `@Autowired` 改为 `@Autowired(required = false)`
2. `@PostConstruct init()` 加 `if (redissonClient != null)` 空判断
3. `addToBlacklist()` / `isInBlacklist()` 加 `bloomFilter != null` 安全检查
4. notification 的 `application.yaml` 排除 `RedissonAutoConfiguration`

### 7.16 修复文件汇总

| 文件 | 问题 | 修复 |
|------|------|------|
| `AuthProperties.java` | `List.of()` 15 参数 | `Arrays.asList()` |
| `AuthProperties.java` | 白名单无 `/yyzx` 前缀 | 双份路径 |
| `BedFeignClient.java` | 含 public 内嵌类 | 提取 `BedDetailsFeignClient` |
| `BedFeignClientFallbackFactory.java` | 含 public 内嵌类 | 提取 `BedDetailsFeignClientFallbackFactory` |
| `SentinelGatewayConfig.java` | Bean 重复定义 | 移除 `sentinelGatewayFilter()` |
| `SentinelGatewayConfig.java` | `BlockRequestHandler` 返回类型 | 改为 Lambda |
| `JwtAuthGlobalFilter.java` | `DatatypeConverter` 不存在 | `getBytes(UTF_8)` |
| `HybridBlacklistUtils.java` | Redisson 强制依赖 | `required=false` + null 检查 |
| `ReportServiceImpl.java` | POI CellType 不兼容 | `CELL_TYPE_STRING` |
| `CustomerController.java` 等 5 个 | `throws Exception` 缺失 | 加回 |

### 7.17 common 模块 Redis/Redisson 强依赖导致无 Redis 服务无法启动（重大问题）

**现象：** bed、checkinout、meal、report、task 等服务启动报 Bean 创建失败：
```
Field redisTemplate in xxx required a bean of type 'RedisTemplate' that could not be found.
Field redissonClient in xxx required a bean of type 'RedissonClient' that could not be found.
```

**根因：** `yyzx-common` 模块中有多个 `@Component` 工具类强依赖 Redis/Redisson，而 `@SpringBootApplication(scanBasePackages = "com.cqupt")` 会扫描并加载它们。但在不需要 Redis 的服务（bed/checkinout/meal/report/task）中，没有 `RedisConfig` 提供 `RedisTemplate<String, Object>`，也没有 `RedissonConfig`

**影响类：**

| 类 | 强依赖 | 修复 |
|-----|--------|------|
| `JwtTokenInterceptor` | RedisTemplate | `@Autowired(required = false)` + null 检查 |
| `HybridBlacklistUtils` | RedissonClient + RedisTemplate | 同 |
| `RedisDelayQueueUtils` | RedisTemplate + ObjectMapper | 同 |
| `RedisLuaUtils` | RedisTemplate | 同 |

**修复方案：** common 中所有 `@Component` 工具类的 Redis/Redisson 依赖全部改为 `@Autowired(required = false)`，并在方法中添加 null 安全检查。同时无 Redis 的 7 个服务配置排除 `RedissonAutoConfiguration`。

> **教训：** common 模块中的 `@Component` 类会被所有依赖它的服务自动加载。对中间件（Redis/RabbitMQ/Nacos）的依赖必须设为 `required = false`，否则会阻塞不需要该中间件的服务启动。

### 7.18 JWT Token 立即过期（YAML key 命名不匹配）

**现象：** 刚登录成功，立刻调用任何接口都返回 `"token 已过期，请重新登录"`

**排查：** 解码 JWT payload 发现 `exp` 为 0（1970-01-01）

**根因：** `application.yaml` 中配置 key 为 `user-ttl`，但 `JwtProperties.java` 中字段名为 `userExpire`。Spring Boot `@ConfigurationProperties` 将 `user-expire` 映射到 `userExpire`，但 `user-ttl` 不匹配，导致 `userExpire = 0`（默认值）。JWT 创建时 `exp = now + 0` → 立即过期

**修复：**
```yaml
# 错误
yyzx.jwt.user-ttl: 7200000

# 正确
yyzx.jwt.user-expire: 7200000   # 匹配 JwtProperties.userExpire
```
修复涉及 auth（创建 JWT）和 gateway/customer/bed/nursing/checkinout/meal（校验 JWT）共 7 个 `application.yaml`。

### 7.19 Gateway 转发不传 `token` Header → JwtTokenInterceptor NPE

**现象：** 请求经 Gateway → customer 后报 `NullPointerException` at `JwtTokenInterceptor.java:70`

**根因：** Gateway 的 `JwtAuthGlobalFilter` 消费 token 后只转发 `X-User-Id` / `X-User-Name` / `X-User-Role`，不传原始 `token` Header。下游服务的 `JwtTokenInterceptor` 读 `token` 为 null → NPE。另：`JwtProperties.userHeader` 字段对应 YAML key `user-header`，但 YAML 写的是 `user-token-name`（不匹配），导致 `getUserHeader()` 也返回 null

**修复：**
1. `JwtTokenInterceptor.preHandle()` 新增 Gateway 信任逻辑：检查 `X-User-Id` header，存在则跳过本地 JWT 校验并直接设置 `BaseContext`
2. 直接使用 `request.getHeader("token")` 字符串常量，而非依赖 `jwtProperties.getUserHeader()`（避免 YAML key 不匹配）

### 7.20 Feign 调用 404 — 缺少 `/yyzx` context-path 前缀

**现象：** `UserFeignClient#getById` → `http://yyzx-auth/internal/admin/user/1` → 404

**根因：** 所有服务配置了 `context-path: /yyzx`，Feign 直连服务时 URL 必须带 `/yyzx` 前缀。但 Feign 的 `path` 属性只写了 `/internal/xxx` 没有 `/yyzx`

**修复：** 所有 5 个 Feign 接口的 `path` 加 `/yyzx` 前缀：
```
/internal/bed  →  /yyzx/internal/bed
/internal/admin →  /yyzx/internal/admin
...等
```

### 7.21 CustomerServiceImpl KhxxFindCustomer 中 `UserFeignClient.getById()` 返回 null → 降级

**现象：** 客户列表查询返回 `flag=false, message="客户服务繁忙，请稍后重试"`（Sentinel fallback）

**根因：** `CustomerServiceImpl.KhxxFindCustomer()` 调用 `userFeignClient.getById(currentUserId)`。Gateway 传入的 `X-User-Id` 是用户 ID（如 1），但 `BaseContext.getCurrentId()` 在 request 属性中获取。JwtTokenInterceptor 修复后通过 `BaseContext.setCurrentId()` 设置，`KhxxFindCustomer` 从中获取。链路已通

**现象：**
```
No Feign Client for loadBalancing defined. Did you forget to include spring-cloud-starter-loadbalancer?
```

**原因：** Spring Cloud 2021.x 中 `spring-cloud-starter-openfeign` 不再自动引入 LoadBalancer。所有使用 Feign 的模块需要手动添加 `spring-cloud-starter-loadbalancer`

**解决：** 在 `yyzx-common/pom.xml` 中添加（所有服务依赖 common，一次覆盖全部）：
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```
