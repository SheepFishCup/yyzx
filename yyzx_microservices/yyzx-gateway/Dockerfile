FROM maven:3.8-openjdk-8-slim AS builder
WORKDIR /build
COPY pom.xml /build/
COPY yyzx-gateway/pom.xml /build/yyzx-gateway/
RUN mvn dependency:go-offline -pl yyzx-gateway -q || true
COPY yyzx-gateway/src /build/yyzx-gateway/src
RUN mvn clean package -pl yyzx-gateway -DskipTests -q

FROM openjdk:8-jre-slim
WORKDIR /app
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# SkyWalking Agent（可选：通过 volume 挂载或构建时复制）
# 使用方式: docker run -v /path/to/skywalking-agent:/skywalking -e SW_ENABLED=true ...
ARG SW_AGENT_ENABLED=false
ENV SW_AGENT_ENABLED=${SW_AGENT_ENABLED} \
    SW_AGENT_NAME=yyzx-gateway \
    SW_BACKEND=localhost:11800

COPY --from=builder /build/yyzx-gateway/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -Xss512k -XX:+DisableExplicitGC -Dfile.encoding=UTF-8 -Duser.timezone=GMT+8"
ENTRYPOINT ["sh", "-c", "\
  if [ \"$SW_AGENT_ENABLED\" = \"true\" ] && [ -f /skywalking/skywalking-agent.jar ]; then \
    echo '[INFO] Starting with SkyWalking Agent: service=$SW_AGENT_NAME backend=$SW_BACKEND'; \
    java -javaagent:/skywalking/skywalking-agent.jar \
      -Dskywalking.agent.service_name=$SW_AGENT_NAME \
      -Dskywalking.collector.backend_service=$SW_BACKEND \
      $JAVA_OPTS -jar app.jar; \
  else \
    java $JAVA_OPTS -jar app.jar; \
  fi"]
