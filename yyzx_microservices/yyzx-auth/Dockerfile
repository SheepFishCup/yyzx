# ==================== 第一阶段：Maven 构建 ====================
FROM maven:3.8-openjdk-8-slim AS builder

WORKDIR /build

# 先复制父 pom 和 common 模块（利用 Docker 缓存层）
COPY pom.xml /build/
COPY yyzx-common/pom.xml /build/yyzx-common/
COPY yyzx-auth/pom.xml /build/yyzx-auth/

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -pl yyzx-auth -am -q || true

# 复制源码
COPY yyzx-common/src /build/yyzx-common/src
COPY yyzx-auth/src /build/yyzx-auth/src

# 构建
RUN mvn clean package -pl yyzx-auth -am -DskipTests -q

# ==================== 第二阶段：运行时镜像 ====================
FROM openjdk:8-jre-slim

WORKDIR /app

# 时区设置
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 复制构建产物
COPY --from=builder /build/yyzx-auth/target/*.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms256m -Xmx512m -Duser.timezone=GMT+8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
