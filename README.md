# Spring Boot 4 + JDK 21 示例项目

这是一个展示 Spring Boot 4 和 JDK 21 新特性的示例项目，包含现代 Java 特性和响应式编程示例。

## 快速启动

### 环境要求
- Java 21
- Gradle 8.x

### 启动应用
```bash
# 克隆项目后，在项目根目录执行：
./gradlew bootRun

# 或使用 Gradle Wrapper (Windows)
gradlew.bat bootRun
```

应用启动后，访问：http://localhost:8080

## 可用接口

### 1. 响应式流接口 (SSE)
```
GET /api/v4/products/stream
```
实时推送产品数据流，每秒推送一个新商品。

**测试方式：**
```bash
curl -N http://localhost:8080/api/v4/products/stream
```
或使用浏览器访问上述地址查看实时数据流。

### 2. 响应式商品接口
```
GET /api/v4/products/{id}
```
获取单个商品信息（模拟延迟100ms的非阻塞响应）。

**示例：**
```bash
curl http://localhost:8080/api/v4/products/123
```

### 3. 用户订单接口
```
GET /api/v4/users/{userId}/orders
```
获取用户的订单列表（响应式流）。

**示例：**
```bash
curl http://localhost:8080/api/v4/users/1/orders
```

### 4. 商品详情接口
```
GET /api/v4/products/{id}/details
```
获取商品详情，包含错误处理示例。

**示例：**
```bash
curl http://localhost:8080/api/v4/products/123/details
```

### 5. 实时通知接口 (SSE)
```
GET /api/v4/notifications
```
服务器发送事件，每2秒推送一次通知。

**测试方式：**
```bash
curl -N http://localhost:8080/api/v4/notifications
```

### 6. 响应式CSV处理
```
GET /api/v4/process-csv
```
模拟响应式处理CSV数据。

**示例：**
```bash
curl http://localhost:8080/api/v4/process-csv
```

## 示例代码运行

### 运行JDK 21特性示例
```bash
# 编译并运行JDK 21特性示例
./gradlew build
java -cp build/classes/java/main com.brian.springstarter.examples.JDK21Features
```

### 运行虚拟线程性能对比
```bash
# 运行快速演示（1000个任务）
java -cp build/classes/java/main com.brian.springstarter.examples.VirtualThreadPerformanceDemo

# 运行完整性能测试（1万个任务）- 需要耐心等待
java -cp build/classes/java/main com.brian.springstarter.examples.VirtualThreadPerformanceDemo --full
```

### 运行JDK 8对比示例
```bash
java -cp build/classes/java/main com.brian.springstarter.examples.JDK8Comparison
```

## 项目结构

```
src/main/java/com/brian/springstarter/
├── SpringStarterApplication.java          # 主应用类
└── examples/
    ├── JDK21Features.java                # JDK 21新特性示例
    ├── SpringBoot4Features.java         # Spring Boot 4响应式示例
    └── JDK8Comparison.java              # JDK 8对比示例
```

## 主要特性展示

### JDK 21 特性
- **Records** - 不可变数据类
- **Pattern Matching** - 模式匹配
- **Virtual Threads** - 虚拟线程（高性能并发）
- **Sequenced Collections** - 序列化集合操作

### Spring Boot 4 特性
- **WebFlux** - 响应式Web框架
- **Server-Sent Events** - 服务器推送事件
- **Reactive Streams** - 响应式数据流
- **Functional Endpoints** - 函数式端点

## 性能优势

- **内存使用**: 1000个虚拟线程 ≈ 1MB vs 传统线程 100MB+
- **代码简洁**: Records减少90%的POJO样板代码
- **并发性能**: 响应式编程支持10K+并发连接
- **开发效率**: 模式匹配消除复杂的if-else链

## 开发命令

```bash
# 构建项目
./gradlew build

# 运行测试
./gradlew test

# 开发模式运行（热部署）
./gradlew bootRun --continuous

# 生成可执行JAR
./gradlew bootJar

# 运行生成的JAR
java -jar build/libs/SpringStarter-0.0.1-SNAPSHOT.jar
```