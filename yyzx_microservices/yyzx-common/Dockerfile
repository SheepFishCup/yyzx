# ==================== yyzx-common 公共模块 Dockerfile ====================
# 此模块为 jar 包依赖，非独立运行服务。
# 用途：在 CI/CD 中预编译 common 模块，供其他服务 Docker 构建时复用（缓存加速）。

FROM maven:3.8-openjdk-8-slim AS builder
WORKDIR /build

# 先复制父 POM（利用 Docker 缓存层）
COPY pom.xml /build/
COPY yyzx-common/pom.xml /build/yyzx-common/

# 下载依赖
RUN mvn dependency:go-offline -pl yyzx-common -q || true

# 复制源码
COPY yyzx-common/src /build/yyzx-common/src

# 编译并安装到本地仓库
RUN mvn clean install -pl yyzx-common -DskipTests -q

# 输出产物
FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=builder /build/yyzx-common/target/*.jar yyzx-common.jar

# 标记为非执行 jar（library）
LABEL module="yyzx-common"
LABEL type="library"
LABEL description="颐养中心微服务公共模块 — 实体、DTO、VO、工具类、Feign 接口"
