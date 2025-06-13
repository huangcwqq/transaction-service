# 阶段 1: 构建阶段
# 使用官方的Eclipse Temurin作为基础镜像，它包含了JDK 21
FROM eclipse-temurin:21-jdk-jammy as builder

# 设置工作目录
WORKDIR /app

# 将Maven的pom.xml和所有源代码复制到容器中
# 这样做的好处是如果pom.xml没有变化，Maven依赖可以被缓存，加快后续构建
COPY pom.xml .
COPY src ./src

# 构建Spring Boot应用程序
# 使用-Dmaven.test.skip=true跳过测试，以加快镜像构建速度。
# 如果需要在构建镜像时运行测试，请移除此参数。
RUN mvn clean install -Dmaven.test.skip=true

# 阶段 2: 运行时阶段
# 使用更轻量级的JaveJRE镜像作为最终运行环境，减小镜像大小
FROM eclipse-temurin:21-jre-jammy

# 设置工作目录
WORKDIR /app

# 从构建阶段复制打包好的JAR文件
# artifactId/version.jar是Maven默认打包的jar文件名
# 假设你的pom.xml中artifactId是transactions，version是0.0.1-SNAPSHOT
COPY --from=builder /app/target/transactions-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用程序运行的端口
EXPOSE 8080

# 定义容器启动时执行的命令
# java -jar app.jar 会启动Spring Boot应用程序
ENTRYPOINT ["java", "-jar", "app.jar"]

# 可以添加健康检查 (可选，但在生产环境中推荐)
# HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
#  CMD curl --fail http://localhost:8080/actuator/health || exit 1
