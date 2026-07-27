<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.cqupt</groupId>
    <artifactId>yyzx-microservices</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>yyzx-microservices</name>
    <description>颐养中心微服务父工程</description>

    <modules>
        <module>yyzx-common</module>
        <module>yyzx-gateway</module>
        <module>yyzx-auth</module>
        <module>yyzx-customer</module>
        <module>yyzx-bed</module>
        <module>yyzx-nursing</module>
        <module>yyzx-checkinout</module>
        <module>yyzx-meal</module>
        <module>yyzx-notification</module>
        <module>yyzx-report</module>
        <module>yyzx-task</module>
    </modules>

    <properties>
        <java.version>1.8</java.version>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <spring-boot.version>2.7.18</spring-boot.version>
        <spring-cloud.version>2021.0.9</spring-cloud.version>
        <spring-cloud-alibaba.version>2021.0.6.1</spring-cloud-alibaba.version>

        <mybatis-plus.version>3.5.1</mybatis-plus.version>
        <mysql.version>8.0.11</mysql.version>
        <druid.version>1.2.6</druid.version>
        <lombok.version>1.18.6</lombok.version>
        <knife4j.version>4.4.0</knife4j.version>
        <redisson.version>3.17.7</redisson.version>
        <jwt.version>3.10.3</jwt.version>
        <fastjson.version>1.2.76</fastjson.version>
        <quartz.version>2.3.2</quartz.version>
        <poi.version>3.16</poi.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Spring Cloud -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Spring Cloud Alibaba -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- 内部公共模块 -->
            <dependency>
                <groupId>com.cqupt</groupId>
                <artifactId>yyzx-common</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- MyBatis-Plus -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <!-- Druid -->
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>druid-spring-boot-starter</artifactId>
                <version>${druid.version}</version>
            </dependency>
            <!-- Knife4j -->
            <dependency>
                <groupId>com.github.xiaoymin</groupId>
                <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
                <version>${knife4j.version}</version>
            </dependency>
            <!-- Redisson -->
            <dependency>
                <groupId>org.redisson</groupId>
                <artifactId>redisson-spring-boot-starter</artifactId>
                <version>${redisson.version}</version>
            </dependency>
            <!-- JWT -->
            <dependency>
                <groupId>com.auth0</groupId>
                <artifactId>java-jwt</artifactId>
                <version>${jwt.version}</version>
            </dependency>
            <!-- Fastjson -->
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>fastjson</artifactId>
                <version>${fastjson.version}</version>
            </dependency>
            <!-- Quartz -->
            <dependency>
                <groupId>org.quartz-scheduler</groupId>
                <artifactId>quartz</artifactId>
                <version>${quartz.version}</version>
            </dependency>
            <dependency>
                <groupId>org.quartz-scheduler</groupId>
                <artifactId>quartz-jobs</artifactId>
                <version>${quartz.version}</version>
            </dependency>
            <!-- POI -->
            <dependency>
                <groupId>org.apache.poi</groupId>
                <artifactId>poi</artifactId>
                <version>${poi.version}</version>
            </dependency>
            <dependency>
                <groupId>org.apache.poi</groupId>
                <artifactId>poi-ooxml</artifactId>
                <version>${poi.version}</version>
            </dependency>
            <!-- HTTP Client -->
            <dependency>
                <groupId>org.apache.httpcomponents</groupId>
                <artifactId>httpclient</artifactId>
                <version>4.5.13</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
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
