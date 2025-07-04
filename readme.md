# 银行交易管理系统

## 概述

本项目是一个简单的交易管理系统，旨在模拟银行交易管理功能。它允许用户创建、查看、修改和删除财务交易。系统使用 Java 21 和 Spring Boot 构建，所有数据均存储在内存中，以简化实现。

## 功能特性

- 用于管理交易的 Restful API
- 内存数据存储（无持久化存储）
- 完善的单元测试和集成测试
- 使用 Docker 进行容器化部署
- 强健的数据验证与异常处理机制
- 高效的数据查询与分页支持

## API 接口

- **GET /api/transactions/token**：获取访问令牌
- **POST /api/transactions**：创建一笔新交易
- **GET /api/transactions**：列出所有交易
- **GET /api/transactions/page**：列出所有交易(分页)
- **GET /api/transactions/{id}**：获取指定 ID 的交易详情
- **PUT /api/transactions/{id}**：更新已有交易
- **DELETE /api/transactions/{id}**：删除指定 ID 的交易

## 快速开始

### 前提条件

- JDK 21 或更高版本
- Maven 3.6 及以上版本
- Docker（可选）

### 安装步骤

1. 克隆仓库：

   ```bash
   git clone <repository-url>
   cd transaction-service
   ```
2. 构建项目:
   ```bash
      mvn clean install
   ```
3. 启动应用:
   ```bash
      mvn spring-boot:run
   ```
### Docker 部署
1. 构建 Docker 镜像:
   ```bash
    docker build -t transaction-service .
   ```
2. 运行 Docker 容器:
   ```bash
    docker run -p 8080:8080 transaction-service
   ```
## 项目结构

- 项目遵循标准的 Maven 结构，主要目录如下：
    - `src/main/java/com/bank/transactions`:
        - **common**：包含 异常类定义。
        - **config**：包含缓存和 swagger配置类，全局异常处理器。
        - **controller**：包含 交易 控制器。
        - **enums**：包含交易类型的枚举类定义。
        - **model**：包含 交易的领域模型。
        - **repository**：包含数据访问层。
        - **request**：接收外部请求的数据模型。
        - **response**：返回外部响应的数据模型。
        - **service**：包含 交易主要业务逻辑。
        - **util**：包含token生成和校验、交易 ID 生成的工具类。
    - `src/test/java/com/bank/transactions`：包含各种测试类。
        - **controller**：包含controller 层的集成测试和模拟的并发测试。
        - **repository**：包含数据访问层的单元测试。
        - **service**：包含service 层业务的单元测试，并发控制测试和缓存机制测试。
        - **util**：包含token生成和校验、交易 ID 生成的工具类的内部方法测试。

## 依赖项

本项目使用了以下库和框架：

### 核心依赖

- **Spring Boot Starter Web**
    - 用于构建 Restful API 并高效处理 HTTP 请求/响应。包含嵌入式 Web 服务器（如 Tomcat）。

- **Spring Boot Starter Validation**
    - 提供对 Jakarta Bean Validation 的支持（例如 `@NotBlank`、`@NotNull`）。用于在处理交易数据前进行输入验证。

- **Lombok**
    - 通过注解（如 `@Data`）自动生成 getter、setter 和构造函数等常见方法，减少样板代码。

- **Spring Boot Starter Data Commons**
    - 提供核心接口（如 `CrudRepository`），用于实现管理交易的内存仓库。

### 测试依赖

- **Spring Boot Starter Test**
    - 集成测试库如 JUnit、Mockito 和 Spring Test，用于编写单元测试和集成测试。

- **JUnit Jupiter API & Engine**
    - 编写 Java 单元测试的基础框架。广泛用于测试套件中定义测试用例和断言。

- **Mockito Core & JUnit Jupiter Integration**
    - 用于在单元测试中模拟依赖项（如服务层或仓库层），确保测试快速且隔离。

### 缓存依赖

- **Spring Boot Starter Cache**
    - 启用应用内的缓存功能，用于提升频繁访问的交易数据的性能。

- **Caffeine**
    - 一个内存缓存库，与 Spring 的缓存抽象集成，用于实现高性能的本地缓存。
### 接口文档
- **Springdoc openapi**
    - 集成 Swagger UI 或 SpringDoc，便于他人快速了解 API 接口
    - Swagger 的接口文档，项目启动后通过访问 `http://localhost:8080/swagger-ui/index.html` 可以查看 API 文档。

##  设计决策

* **分层架构:** 采用了经典的 Controller -> Service -> Repository 三层架构，确保职责分离，提高代码可维护性和可测试性。

* **内存数据存储:** 根据题目要求，使用 `java.util.concurrent.ConcurrentHashMap` 在内存中存储交易数据。`ConcurrentHashMap` 保证了在多线程环境下的数据安全性和高效访问。

* **Restful API:** 严格遵循 Restful 原则设计 API 端点，使用标准的 HTTP 方法和状态码。

* **DTOs:** 使用 `CreateTransactionRequest`、`UpdateTransactionRequest` 和 `TransactionResponse` 作为数据传输对象，将领域模型与外部接口解耦，并便于数据验证。

* **数据验证:** 利用 Spring Boot Starter Validation (`jakarta.validation.constraints`) 在 DTO 层进行输入验证，确保数据质量。

* **全局异常处理:** 通过 `@ControllerAdvice` 和 `@ExceptionHandler` 实现了统一的全局异常处理，所有业务和验证错误都会返回一致的 JSON 错误响应，提升 API 的健壮性和用户体验。

* **业务异常:** 定义了 `DuplicateRequestException`、InvalidRequestException` 和 `TransactionNotFoundException` 等自定义业务和请求异常，使错误类型更具语义化。

* **分页与排序:** 在 `GET /api/transactions/page` 接口中实现了内存数据的分页和排序功能，展示了对大型数据集处理的考虑，即使在内存场景下也能保证查询效率和灵活性。

* **Java 21:** 项目基于 Java 21 构建，利用了最新的语言特性和性能优化。
## 测试策略

项目包含全面的测试，以确保代码质量、逻辑正确性和 API 行为符合预期。

* **完善的单元测试 (`TransactionServiceTest等`):**

    * **目标:** 独立测试 `TransactionService` 中的业务逻辑。

    * **技术:** JUnit 5 和 Mockito。

    * **覆盖:** 涵盖所有业务方法的成功路径和各种异常路径（如查找不到、重复创建等）。

    * **隔离性:** 通过 Mockito 模拟 `TransactionRepository`，确保 `TransactionService` 的测试不依赖于实际的内存存储实现。

* **集成测试 (`TransactionControllerTest`):**

    * **目标:** 测试从 HTTP 请求到控制器、服务层直至内存仓库的整个请求处理流程。

    * **技术:** Spring Boot Test 和 MockMvc。

    * **覆盖:** 涵盖所有 REST API 端点，验证 HTTP 状态码、响应体内容以及异常处理是否正确。

* **压力测试（StressTest）:**
    * **目标:** 测试每个API对外提供服务的能力。

    * **技术:** JUnit 5 和 虚拟线程线程池。

    * **覆盖:** 涵盖 API 方法的调用。

    * **提升:** 这不是专业的服务器压力测试，只是模拟了高并发下的接口调用，真正的压测是需要外部使用 Jmeter 等工具进行的。
## 潜在改进和未来工作
* **更复杂的查询:** 增加按金额范围、日期范围等进行交易查询的功能。

* **压力测试:** 使用 Jmeter 模拟真实的高并发场景，以覆盖高并发的业务请求场景。

* **身份验证与授权:** 实现用户认证和基于角色的访问控制，以满足实际银行系统的安全要求。

* **持久化存储:** 引入实际的数据库（如 H2, PostgreSQL, MySQL）来替代内存存储，并使用 Spring Data JPA 进行数据访问。

* **缓存机制:** 引入 Redis 分布式缓存，以提高常用数据的读取性能和缓存稳定性。

* **Metrics 和监控:** 集成 Micrometer 或 Prometheus 等工具，用于收集和暴露应用程序的运行时指标。
