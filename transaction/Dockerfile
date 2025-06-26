# 构建阶段，基于带 Maven 的 JDK 21 alpine 镜像
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 复制pom.xml，利用依赖缓存
COPY pom.xml .

# 复制源码
COPY src ./src

# 编译打包，跳过测试
RUN mvn clean package -DskipTests

# 运行阶段，基于轻量JDK 21 alpine镜像
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# 从构建阶段复制打包好的jar文件
COPY --from=builder /app/target/transaction-1.0-SNAPSHOT-jar-with-dependencies.jar  app.jar

# 容器对外暴露8080端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]

