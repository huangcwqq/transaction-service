```markdown
# HSBC 交易管理家庭作业

## 1. 项目概述

这是一个为汇丰软件面试准备的家庭作业项目，旨在模拟一个简单的银行交易管理系统。该应用程序允许用户进行交易的创建、查看、修改和删除操作。所有数据都存储在内存中，不涉及持久化存储。

**核心要求：**

* 使用 Java 21 和 Spring Boot。

* 主要实体为 `Transaction`。

* 数据存储在内存中，无持久化需求。

* 提供清晰、高性能、良好结构的 RESTful API。

* 包含全面的单元测试和集成测试。

* 支持容器化（Docker）。

## 2. 技术栈

* **后端框架:** Spring Boot 3.3.1

* **编程语言:** Java 21

* **构建工具:** Maven

* **依赖管理:** Lombok (简化 POJO 代码)

* **数据验证:** Jakarta Validation (JSR 303)

* **测试框架:** JUnit 5, Mockito, Spring Boot Test

* **容器化:** Docker

## 3. 项目结构

```
.
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── hsbc
│   │   │           └── transactions
│   │   │               ├── config                  // 配置类
│   │   │               ├── controller              // REST API 控制器
│   │   │               ├── dto                     // 数据传输对象 (请求/响应)
│   │   │               ├── exception               // 自定义异常和全局异常处理
│   │   │               ├── model                   // 领域模型
│   │   │               ├── repository              // 数据访问层 (内存实现)
│   │   │               ├── service                 // 业务逻辑层
│   │   │               └── TransactionsApplication.java // Spring Boot 应用启动类
│   │   └── resources
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── hsbc
│                   └── transactions
│                       ├── controller
│                       │   └── TransactionControllerIntegrationTest.java // 集成测试
│                       └── service
│                           └── TransactionServiceTest.java             // 单元测试
├── Dockerfile              // Docker 镜像构建文件
├── pom.xml                 // Maven 项目配置文件
└── README.md               // 项目说明文档 (当前文件)

```

## 4. 如何构建和运行

### 4.1 前置条件

* Java Development Kit (JDK) 21 或更高版本

* Apache Maven 3.6+

* Docker Desktop (如果要在Docker中运行)

### 4.2 本地运行

1. **克隆仓库:**

   ```
git clone <您的GitHub仓库URL>
cd transactions

   ```

2. **构建项目:**

   ```
mvn clean install

   ```

3. **运行应用程序:**

   ```
mvn spring-boot:run

   ```

   或者运行生成的 JAR 包：

   ```
java -jar target/transactions-0.0.1-SNAPSHOT.jar

   ```

应用程序将在 `http://localhost:8080` 启动。

### 4.3 使用 Docker 运行

1. **构建 Docker 镜像:**
   在项目根目录（`pom.xml` 和 `Dockerfile` 所在目录）下执行：

   ```
docker build -t hsbc-transactions-app .

   ```

2. **运行 Docker 容器:**

   ```
docker run -p 8080:8080 hsbc-transactions-app

   ```

   应用程序将在 Docker 容器内部的 8080 端口启动，并映射到宿主机的 8080 端口。

## 5. API 文档

所有 API 端点的前缀都是 `/api/transactions`。

### 5.1 创建交易 (POST)

* **URL:** `/api/transactions`

* **方法:** `POST`

* **请求体 (Content-Type: application/json):**

  ```
{
"amount": 100.50,
"type": "DEPOSIT", // 或 "WITHDRAWAL"
"description": "工资入账"
}

  ```

* **响应 (201 Created):**

  ```
{
"id": "e1a2b3c4-d5e6-7890-1234-56789abcdef0",
"amount": 100.50,
"type": "DEPOSIT",
"date": "2024-06-12T23:30:00.123456",
"description": "工资入账"
}

  ```

* **错误响应 (400 Bad Request):**

  ```
{
"timestamp": "2024-06-12T23:35:00.123456",
"status": 400,
"error": "Bad Request",
"message": "请求参数验证失败: amount: 金额必须大于0; type: 交易类型不能为空"
}

  ```

### 5.2 获取所有交易 (GET)

* **URL:** `/api/transactions`

* **方法:** `GET`

* **查询参数 (可选):**

  * `page`: 页码 (默认为 0)

  * `size`: 每页大小 (默认为 10)

  * `sortBy`: 排序字段 (默认为 "date"。支持 "date", "amount", "type")

  * `sortDir`: 排序方向 (默认为 "desc"。支持 "asc", "desc")

* **示例:** `/api/transactions?page=0&size=5&sortBy=amount&sortDir=asc`

* **响应 (200 OK):**

  ```
[
{
"id": "id1",
"amount": 50.00,
"type": "WITHDRAWAL",
"date": "2024-06-12T23:00:00",
"description": "咖啡"
},
{
"id": "id2",
"amount": 100.00,
"type": "DEPOSIT",
"date": "2024-06-11T10:00:00",
"description": "午餐"
}
]

  ```

### 5.3 获取单个交易 (GET)

* **URL:** `/api/transactions/{id}`

* **方法:** `GET`

* **响应 (200 OK):**

  ```
{
"id": "e1a2b3c4-d5e6-7890-1234-56789abcdef0",
"amount": 100.50,
"type": "DEPOSIT",
"date": "2024-06-12T23:30:00.123456",
"description": "工资入账"
}

  ```

* **错误响应 (404 Not Found):**

  ```
{
"timestamp": "2024-06-12T23:35:00.123456",
"status": 404,
"error": "Not Found",
"message": "交易未找到，ID: non-existent-id"
}

  ```

### 5.4 更新交易 (PUT)

* **URL:** `/api/transactions/{id}`

* **方法:** `PUT`

* **请求体 (Content-Type: application/json):**

  ```
{
"amount": 120.00,
"type": "DEPOSIT",
"description": "更新后的工资入账"
}

  ```

* **响应 (200 OK):** (与创建交易成功响应类似，但包含更新后的数据)

* **错误响应 (400 Bad Request, 404 Not Found):** (与创建交易、获取单个交易的错误响应类似)

### 5.5 删除交易 (DELETE)

* **URL:** `/api/transactions/{id}`

* **方法:** `DELETE`

* **响应 (204 No Content):** (成功删除后无响应体)

* **错误响应 (404 Not Found):**

  ```
{
"timestamp": "2024-06-12T23:35:00.123456",
"status": 404,
"error": "Not Found",
"message": "无法删除，交易未找到，ID: non-existent-id"
}

  ```

## 6. 设计决策

* **分层架构:** 采用了经典的 Controller -> Service -> Repository 三层架构，确保职责分离，提高代码可维护性和可测试性。

* **内存数据存储:** 根据题目要求，使用 `java.util.concurrent.ConcurrentHashMap` 在内存中存储交易数据。`ConcurrentHashMap` 保证了在多线程环境下的数据安全性和高效访问。

* **RESTful API:** 严格遵循 RESTful 原则设计 API 端点，使用标准的 HTTP 方法和状态码。

* **DTOs:** 使用 `TransactionRequest` 和 `TransactionResponse` 作为数据传输对象，将领域模型与外部接口解耦，并便于数据验证。

* **数据验证:** 利用 Spring Boot Starter Validation (`jakarta.validation.constraints`) 在 DTO 层进行输入验证，确保数据质量。

* **全局异常处理:** 通过 `@ControllerAdvice` 和 `@ExceptionHandler` 实现了统一的全局异常处理，所有业务和验证错误都会返回一致的 JSON 错误响应，提升 API 的健壮性和用户体验。

* **业务异常:** 定义了 `DuplicateTransactionException` 和 `TransactionNotFoundException` 等自定义业务异常，使错误类型更具语义化。

* **分页与排序:** 在 `GET /api/transactions` 接口中实现了内存数据的分页和排序功能，展示了对大型数据集处理的考虑，即使在内存场景下也能保证查询效率和灵活性。

* **Java 21:** 项目基于 Java 21 构建，利用了最新的语言特性和性能优化。

## 7. 测试策略

项目包含全面的测试，以确保代码质量、逻辑正确性和 API 行为符合预期。

* **单元测试 (`TransactionServiceTest`):**

  * **目标:** 独立测试 `TransactionService` 中的业务逻辑。

  * **技术:** JUnit 5 和 Mockito。

  * **覆盖:** 涵盖所有业务方法的成功路径和各种异常路径（如查找不到、重复创建等）。

  * **隔离性:** 通过 Mockito 模拟 `TransactionRepository`，确保 `TransactionService` 的测试不依赖于实际的内存存储实现。

* **集成测试 (`TransactionControllerIntegrationTest`):**

  * **目标:** 测试从 HTTP 请求到控制器、服务层直至内存仓库的整个请求处理流程。

  * **技术:** Spring Boot Test 和 MockMvc。

  * **覆盖:** 涵盖所有 REST API 端点，验证 HTTP 状态码、响应体内容以及异常处理是否正确。

  * **数据隔离:** 每个测试方法前会清空内存仓库，确保测试间的独立性。

* **压力测试考虑:**
  虽然本项目未包含自动化压力测试脚本（通常由外部工具完成），但设计和实现上已充分考虑性能：

  * **内存数据存储:** `ConcurrentHashMap` 提供了高效的并发读写能力。

  * **无状态 API:** RESTful API 的无状态特性天然支持水平扩展。

  * **简洁的业务逻辑:** 避免了不必要的复杂计算。
    这些设计选择为未来进行专业压力测试（例如使用 JMeter）奠定了良好的基础。

## 8. 外部库

除了 Spring Boot 提供的核心依赖和 JUnit/Mockito 等测试依赖外，本项目还使用了：

* **Lombok:** 用于自动生成 POJO 的 boilerplate 代码（如 getter, setter, 构造函数等），极大地简化了代码，提高了可读性。在 `pom.xml` 中配置为 `optional`，表示它在编译时有用，但在运行时不需要作为传递依赖。

## 9. 潜在改进和未来工作

* **更复杂的查询:** 增加按金额范围、日期范围等进行交易查询的功能。

* **身份验证与授权:** 实现用户认证和基于角色的访问控制，以满足实际银行系统的安全要求。

* **持久化存储:** 引入实际的数据库（如 H2, PostgreSQL, MySQL）来替代内存存储，并使用 Spring Data JPA 进行数据访问。

* **缓存机制:** 引入 Guava Cache 或 Spring Cache 等本地缓存，或 Redis 等分布式缓存，以提高常用数据的读取性能。

* **API 版本控制:** 随着 API 的演进，实现版本控制策略（例如，通过 URL 或 Header）。

* **API 文档工具:** 集成 Swagger/OpenAPI 来自动生成和维护 API 文档。

* **Metrics 和监控:** 集成 Micrometer 或 Prometheus 等工具，用于收集和暴露应用程序的运行时指标。
```