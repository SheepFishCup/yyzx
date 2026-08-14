# JVM 参数优化方案

> 颐养中心微服务 — 各服务 JVM 内存与 GC 调优

---

## 1. 优化总览

| 服务 | 堆内存 | 特殊说明 |
|------|--------|---------|
| yyzx-gateway | 256m ~ 512m | 高并发路由，Netty 非阻塞 |
| yyzx-auth | 256m ~ 512m | 登录密集，Redis 分担 |
| **yyzx-customer** | **512m ~ 1g** | **缓存密集 + Feign 调用频繁** |
| yyzx-bed | 256m ~ 512m | 轻量 CRUD |
| yyzx-nursing | 256m ~ 512m | Redis Lua 分担计算 |
| yyzx-checkinout | 256m ~ 512m | Feign 调用方 |
| yyzx-meal | 256m ~ 512m | 轻量 CRUD + 图片上传 |
| yyzx-notification | 256m ~ 512m | MQ 消费者 |
| **yyzx-report** | **512m ~ 1g** | **Excel 导出内存峰值高** |
| yyzx-task | 256m ~ 512m | 定时任务轻量 |

---

## 2. 统一 JVM 参数

```bash
-Xms256m -Xmx512m                          # 堆内存（按服务差异化）
-XX:+UseG1GC                               # G1 垃圾回收器（Java 8 最优）
-XX:MaxGCPauseMillis=200                    # 最大 GC 停顿 200ms
-XX:+HeapDumpOnOutOfMemoryError             # OOM 时自动生成堆转储
-XX:HeapDumpPath=./logs/heapdump.hprof      # 堆转储路径
-XX:MetaspaceSize=128m                      # 元空间初始大小
-XX:MaxMetaspaceSize=256m                   # 元空间上限（防泄漏）
-Xss512k                                    # 线程栈 512k（微服务线程多）
-XX:+DisableExplicitGC                      # 禁用 System.gc()
-Dfile.encoding=UTF-8                       # 统一 UTF-8 编码
-Duser.timezone=GMT+8                       # 时区
```

---

## 3. 参数详解

### 3.1 G1GC 选择理由

| 收集器 | 特点 | 适用 |
|--------|------|------|
| Serial | 单线程，停顿长 | 不适用 |
| Parallel | 吞吐优先，停顿 100ms+ | 批处理 |
| CMS | 并发标记，碎片多 | 已废弃 |
| **G1** ✅ | **可预测停顿 + 无碎片 + 自动分代** | **微服务首选** |

`-XX:MaxGCPauseMillis=200`：G1 承诺单次 GC 停顿不超过 200ms，满足 API 响应要求。

### 3.2 元空间（Metaspace）

Spring Boot + 微服务框架类多，默认元空间上限 256m 可能不够。显式设置：
- 初始 128m — 避免频繁扩容
- 上限 256m — 防止 ClassLoader 泄漏吃光内存

### 3.3 线程栈 512k

Tomcat 默认 200 线程 × 1M 栈 = 200M 栈内存。降到 512k 可省 100M，微服务方法调用深度浅，512k 足够。

### 3.4 HeapDump 路径

OOM 时自动生成 `./logs/heapdump.hprof`，用 JProfiler/MAT 分析内存泄漏。

---

## 4. 各文件改动位置

| 位置 | 文件 |
|------|------|
| Windows 启动脚本 | `scripts/start-*.bat`（10 个） |
| Linux 启动脚本 | `scripts/start-with-skywalking.sh` |
| Docker 镜像 | 各服务 `Dockerfile` 的 `ENV JAVA_OPTS` |

Docker 中 HeapDump 路径为 `/app/logs/heapdump.hprof`（容器内路径）。

---

## 5. 监控与验证

### 5.1 查看运行参数

```bash
# Windows
wmic process where "name='java.exe'" get CommandLine | findstr yyzx

# 或 jcmd
jcmd <PID> VM.flags
```

### 5.2 验证 GC 类型

```bash
jinfo <PID> | findstr UseG1GC
# 预期: -XX:+UseG1GC
```

### 5.3 GC 日志（可选追加）

```bash
# 需要更详细 GC 分析时追加：
-Xloggc:./logs/gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps
```

### 5.4 内存占用参考

| 场景 | 预估内存 |
|------|---------|
| 10 服务全部运行 | 2.5G ~ 4G |
| 加中间件（MySQL/Redis/RabbitMQ/Nacos） | 4G ~ 6G |
| 加 SkyWalking OAP + Sentinel | 5G ~ 7G |

> 开发机建议 16G 内存；8G 机器建议将各服务 `-Xmx` 下调一档（如 512m→384m）。
