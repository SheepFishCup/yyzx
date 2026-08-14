FROM maven:3.8-openjdk-8-slim AS builder
WORKDIR /build
COPY pom.xml /build/
COPY yyzx-common/pom.xml /build/yyzx-common/
COPY yyzx-nursing/pom.xml /build/yyzx-nursing/
RUN mvn dependency:go-offline -pl yyzx-nursing -am -q || true
COPY yyzx-common/src /build/yyzx-common/src
COPY yyzx-nursing/src /build/yyzx-nursing/src
RUN mvn clean package -pl yyzx-nursing -am -DskipTests -q

FROM openjdk:8-jre-slim
WORKDIR /app
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone
COPY --from=builder /build/yyzx-nursing/target/*.jar app.jar
EXPOSE 8084
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -Xss512k -XX:+DisableExplicitGC -Dfile.encoding=UTF-8 -Duser.timezone=GMT+8"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
