# 第一阶段：使用 Maven 镜像构建项目
FROM maven:3.8-openjdk-8-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 文件（利用 Docker 缓存层）
COPY pom.xml .

# 复制源代码
COPY src ./src

# 执行 Maven 打包命令
RUN mvn clean package -DskipTests

# 第二阶段：使用 JRE 运行应用
FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制打包好的 jar 文件
COPY --from=builder /app/target/yyzx_backend-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 9999

# 设置时区
ENV TZ=Asia/Shanghai

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Duser.timezone=GMT+8"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
