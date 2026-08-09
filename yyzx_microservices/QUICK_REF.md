# 一页速查表

> 颐养中心微服务 — 常用命令、端口、配置速查

---

## 1. 模块速查

| 模块 | 端口 | 基础包 | 启动命令 |
|------|------|--------|---------|
| yyzx-common | — | `com.cqupt.*` | `mvn clean install -pl yyzx-common -DskipTests` |
| yyzx-gateway | 8080 | `com.cqupt.gateway` | `mvn spring-boot:run -pl yyzx-gateway` |
| yyzx-auth | 8081 | `com.cqupt.auth` | `mvn spring-boot:run -pl yyzx-auth` |
| yyzx-customer | 8082 | `com.cqupt.customer` | `mvn spring-boot:run -pl yyzx-customer` |
| yyzx-bed | 8083 | `com.cqupt.bed` | `mvn spring-boot:run -pl yyzx-bed` |
| yyzx-nursing | 8084 | `com.cqupt.nursing` | `mvn spring-boot:run -pl yyzx-nursing` |
| yyzx-checkinout | 8085 | `com.cqupt.checkinout` | `mvn spring-boot:run -pl yyzx-checkinout` |
| yyzx-meal | 8086 | `com.cqupt.meal` | `mvn spring-boot:run -pl yyzx-meal` |
| yyzx-notification | 8087 | `com.cqupt.notification` | `mvn spring-boot:run -pl yyzx-notification` |
| yyzx-report | 8088 | `com.cqupt.report` | `mvn spring-boot:run -pl yyzx-report` |
| yyzx-task | 8089 | `com.cqupt.task` | `mvn spring-boot:run -pl yyzx-task` |

---

## 2. 健康检查地址

| 服务 | Knife4j 文档 | Druid 监控 |
|------|-------------|-----------|
| Gateway | `http://localhost:8080/yyzx/doc.html` | — |
| Auth | `http://localhost:8081/yyzx/doc.html` | `http://localhost:8081/yyzx/druid` |
| Customer | `http://localhost:8082/yyzx/doc.html` | `http://localhost:8082/yyzx/druid` |
| Bed | `http://localhost:8083/yyzx/doc.html` | `http://localhost:8083/yyzx/druid` |
| Nursing | `http://localhost:8084/yyzx/doc.html` | `http://localhost:8084/yyzx/druid` |
| CheckInOut | `http://localhost:8085/yyzx/doc.html` | `http://localhost:8085/yyzx/druid` |
| Meal | `http://localhost:8086/yyzx/doc.html` | `http://localhost:8086/yyzx/druid` |
| Notification | `http://localhost:8087/yyzx/doc.html` | `http://localhost:8087/yyzx/druid` |
| Report | `http://localhost:8088/yyzx/doc.html` | `http://localhost:8088/yyzx/druid` |
| Task | `http://localhost:8089/yyzx/doc.html` | `http://localhost:8089/yyzx/druid` |

---

## 3. 中间件连接地址

| 中间件 | 地址 | 默认账号/密码 |
|--------|------|-------------|
| MySQL | `localhost:3306` | root / 123456 |
| Redis | `localhost:6379` | — / yourpassword |
| RabbitMQ | `localhost:5672` | guest / guest |
| RabbitMQ 管理 | `http://localhost:15672` | guest / guest |
| Nacos | `http://localhost:8848/nacos` | nacos / nacos |
| Sentinel Dashboard | `http://localhost:8090` | sentinel / sentinel |
| SkyWalking UI | `http://localhost:8085` | — |

---

## 4. 核心技术栈版本

| 技术 | 版本 |
|------|------|
| Java | 1.8 |
| Spring Boot | 2.7.18 |
| Spring Cloud | 2021.0.9 |
| Spring Cloud Alibaba | 2021.0.6.1 |
| Nacos | 2.2.3 |
| MyBatis-Plus | 3.5.1 |
| Druid | 1.2.6 |
| MySQL Connector | 8.0.11 |
| Redisson | 3.17.7 |
| JJWT | 0.9.1 |
| Auth0 JWT | 3.10.3 |
| Lombok | 1.18.6 |
| Fastjson | 1.2.76 |
| Knife4j | 4.4.0 |
| Quartz | 2.3.2 |
| POI | 3.16 |
| Kaptcha | 2.3.2 |
| DingTalk SDK | 2.0.0 |
| Apache HttpClient | 4.5.13 |
| OpenFeign | 3.1.9 |
| Resilience4j | 2.1.7 |
| Sentinel | (via SCA) |
| SkyWalking Agent (Java) | 9.3.0 |
| Maven | 3.6.3+ |
| Maven Compiler Plugin | 3.8.1 |

---

## 5. 常用 Maven 命令

```bash
# 编译
mvn clean compile                              # 全部编译
mvn clean install -pl yyzx-common -DskipTests  # 安装 common
mvn clean package -pl yyzx-auth -DskipTests    # 打包单模块

# 运行
mvn spring-boot:run -pl yyzx-auth              # 启动单服务

# 测试
mvn test                                       # 全部测试
mvn test -pl yyzx-auth                         # 单模块测试
mvn test -Dtest=YyzxAuthApplicationTests       # 单测试类

# 依赖
mvn dependency:tree -pl yyzx-auth              # 查看依赖树
mvn versions:display-dependency-updates        # 检查依赖更新
```

---

## 6. 常用 Docker 命令

```bash
# 构建与启动
docker-compose up -d --build                   # 构建并后台启动
docker-compose up -d                           # 仅启动（不重新构建）
docker-compose down                            # 停止并删除容器

# 日志
docker-compose logs -f                         # 全部日志（实时）
docker-compose logs -f yyzx-gateway            # 单服务日志
docker-compose logs --tail=100 yyzx-auth       # 最近 100 行

# 重启
docker-compose restart yyzx-auth               # 重启单服务
docker-compose down && docker-compose up -d    # 完全重建

# 进入容器
docker exec -it yyzx-auth sh                   # 进入容器 shell
```

---

## 7. Git 常用命令

```bash
git status                                      # 查看状态
git checkout -b feature/xxx                     # 创建功能分支
git add . && git commit -m "feat(auth): xxx"   # 提交
git push origin feature/xxx                     # 推送
git merge --no-ff feature/xxx                   # 合并（保留分支历史）
```

---

## 8. 快速调试命令

```bash
# Redis
redis-cli                                      # 连接 Redis
> KEYS *                                        # 查看所有 key
> GET user:token:1                              # 查看用户 token
> TTL user:token:1                              # 查看 token 剩余时间
> FLUSHDB                                       # 清空当前数据库

# MySQL
mysql -u root -p123456 yyzx                     # 连接
> SHOW TABLES;                                  # 查看所有表
> SELECT * FROM user WHERE username='admin';    # 查询用户
> SELECT * FROM backdown WHERE audit_status=0;  # 待审批退住

# RabbitMQ
# 管理界面: http://localhost:15672
# 查看队列消息数、清空队列、发送测试消息

# Nacos
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=yyzx-auth
```

---

## 9. 配置文件位置

| 配置 | 文件路径 |
|------|---------|
| 父 POM | `yyzx_microservices/pom.xml` |
| Docker Compose | `yyzx_microservices/docker-compose.yml` |
| 环境变量 | `yyzx_microservices/.env` |
| Nacos 配置 | `yyzx_microservices/nacos-config/*.yaml` |
| 各服务本地配置 | `yyzx_<模块>/src/main/resources/application.yaml` |
| 各服务 Docker 配置 | `yyzx_<模块>/src/main/resources/application-docker.yaml` |
| 各服务 Mapper XML | `yyzx_<模块>/src/main/resources/mapper/*.xml` |

---

## 10. 接口快速测试

```bash
GW="http://localhost:8080"

# 1. 获取验证码
curl -s $GW/admin/generate | python -m json.tool

# 2. 登录
TOKEN=$(curl -s -X POST $GW/admin/loginWithCaptcha \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456","captcha":"1234","uuid":"xxx"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

# 3. 查询客户
curl -s "$GW/customer/listKhxxPage?current=1&pageSize=5" \
  -H "token: $TOKEN" | python -m json.tool

# 4. 客户统计
curl -s "$GW/report/customerStats?startDate=2026-06-21&endDate=2026-07-21" \
  -H "token: $TOKEN" | python -m json.tool

# 5. 菜品列表
curl -s $GW/food/listFood -H "token: $TOKEN" | python -m json.tool
```

---

## 11. 文件数量速查

| 类型 | 数量 |
|------|------|
| Java 源文件 | 275 |
| MyBatis XML | 38 |
| YAML 配置 | 33 |
| 单元测试 | 10 |
| Dockerfile | 11 |
| pom.xml | 12 |
| Nacos 配置 | 11 |
| 启动脚本 | 6 |
| 文档 | 11 份 .md |
| **总计** | **~410 文件** |

---

## 12. 登录 & 测试速查

```powershell
# 首次登录（获取 token，自动弹验证码图片）
.\scripts\login.ps1

# 新终端恢复 token（2 小时内有效）
. .\scripts\token-init.ps1

# 快捷测试
call-api '/admin/generate'                           # 无需 token
call-api '/admin/findUserPage?current=1'             # 需 token
call-api '/customer/listKhxxPage?current=1&pageSize=5'
call-api '/bed/findBed'
call-api '/room/listRoom'
call-api '/food/listFood'
call-api '/nurselevel/listNurseLevel'
call-api '/report/customerStats?startDate=2026-07-02&endDate=2026-08-01'
```

---

## 13. SkyWalking + Sentinel 启动速查

```bash
# Sentinel Dashboard
java -jar sentinel-dashboard.jar --server.port=8080

# 带 SkyWalking Agent 启动（无需 Agent 则忽略 -javaagent）
mvn spring-boot:run -pl yyzx-gateway \
  -Dspring-boot.run.jvmArguments="-javaagent:/opt/skywalking-agent/skywalking-agent.jar -Dskywalking.agent.service_name=yyzx-gateway -Dskywalking.collector.backend_service=localhost:11800"

# 用脚本启动
scripts/start-with-skywalking.sh gateway   # Linux/Mac
scripts\start-gateway.bat                  # Windows
```

## 13. 新增文件清单（高可用升级）

```
scripts/
├── skywalking-agent.config
├── start-gateway.bat / start-auth.bat / start-customer.bat
└── start-with-skywalking.sh

yyzx-common/.../feign/
├── BedFeignClientFallbackFactory.java
├── CustomerFeignClientFallbackFactory.java
└── UserFeignClientFallbackFactory.java

yyzx-gateway/.../config/
└── SentinelGatewayConfig.java

yyzx-customer/.../config/
└── SentinelRulesConfig.java

yyzx-checkinout/.../controller/
└── CheckinoutInternalController.java
```
