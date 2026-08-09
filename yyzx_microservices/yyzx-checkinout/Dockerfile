FROM maven:3.8-openjdk-8-slim AS builder
WORKDIR /build
COPY pom.xml /build/
COPY yyzx-common/pom.xml /build/yyzx-common/
COPY yyzx-checkinout/pom.xml /build/yyzx-checkinout/
RUN mvn dependency:go-offline -pl yyzx-checkinout -am -q || true
COPY yyzx-common/src /build/yyzx-common/src
COPY yyzx-checkinout/src /build/yyzx-checkinout/src
RUN mvn clean package -pl yyzx-checkinout -am -DskipTests -q

FROM openjdk:8-jre-slim
WORKDIR /app
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone
COPY --from=builder /build/yyzx-checkinout/target/*.jar app.jar
EXPOSE 8085
ENV JAVA_OPTS="-Xms256m -Xmx512m -Duser.timezone=GMT+8"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
