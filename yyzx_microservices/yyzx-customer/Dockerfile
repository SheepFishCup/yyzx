FROM maven:3.8-openjdk-8-slim AS builder
WORKDIR /build
COPY pom.xml /build/
COPY yyzx-common/pom.xml /build/yyzx-common/
COPY yyzx-customer/pom.xml /build/yyzx-customer/
RUN mvn dependency:go-offline -pl yyzx-customer -am -q || true
COPY yyzx-common/src /build/yyzx-common/src
COPY yyzx-customer/src /build/yyzx-customer/src
RUN mvn clean package -pl yyzx-customer -am -DskipTests -q

FROM openjdk:8-jre-slim
WORKDIR /app
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone
COPY --from=builder /build/yyzx-customer/target/*.jar app.jar
EXPOSE 8082
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -Xss512k -XX:+DisableExplicitGC -Dfile.encoding=UTF-8 -Duser.timezone=GMT+8"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
