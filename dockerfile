# 阶段 1: 构建阶段
FROM registry.cn-hangzhou.aliyuncs.com/dragonwell/dragonwell-21:latest as builder

WORKDIR /app

# 安装 Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# 复制依赖文件以利用缓存
COPY pom.xml .
# 如有 settings.xml 可一并复制
# COPY conf/maven/settings.xml ./
RUN mvn dependency:go-offline

# 复制源码
COPY src ./src

# 构建 Spring Boot 应用
RUN mvn clean package -Dmaven.test.skip=true

# 阶段 2: 运行时阶段
FROM registry.cn-hangzhou.aliyuncs.com/dragonwell/dragonwell-21:latest

WORKDIR /app

# 支持自定义 JAVA_OPTS
ENV JAVA_OPTS=""

# 应用标识和元数据
ENV Bank_APPLICATION=transaction-service
LABEL bank-app="$bank_APPLICATION"

# 复制构建产物，自动识别 jar
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

# 健康检查（可选）
# HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
#   CMD curl --fail http://localhost:8080/actuator/health || exit 1

