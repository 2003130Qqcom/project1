# HMall — 飞猪电商微服务项目

基于 **Spring Cloud Alibaba** 的电商微服务实战项目，拆分多个独立子服务，涵盖商品、购物车、订单、支付、用户等核心业务模块。服务间通信采用 **Feign（同步）+ RabbitMQ（异步）**混合架构。

---

## 📋 项目概览

| 属性 | 说明 |
|------|------|
| 架构风格 | 微服务（Spring Cloud） |
| JDK | 11 |
| Spring Boot | 2.7.12 |
| Spring Cloud | 2021.0.3 |
| Spring Cloud Alibaba | 2021.0.4.0 |
| 数据库 | MySQL 8.0（每服务独立库） |
| ORM | MyBatis-Plus 3.4.3 |
| 注册中心 | Nacos |
| 网关 | Spring Cloud Gateway |
| 服务调用 | OpenFeign（同步）+ RabbitMQ（异步） |
| 接口文档 | Knife4j (Swagger) |

---

## 🏗️ 模块结构

```
hmall/
├── hm-gateway/          # 统一网关 (端口 8080)
│   └── 路由转发、JWT 登录校验、鉴权拦截
│
├── hm-common/           # 公共模块（工具类、异常、统一返回体、MQ 常量）
│   ├── 统一返回体 R<T>
│   ├── 全局异常处理 CommonExceptionAdvice
│   ├── 分页查询 PageQuery / PageDTO
│   ├── MQ 配置 / 消息体定义 / 常量
│   └── 工具类 BeanUtils、CollUtils、UserContext 等
│
├── hm-api/              # Feign 接口定义（服务间同步调用 API）
│   ├── ItemClient       # 商品服务调用
│   ├── CartClient       # 购物车服务调用
│   ├── TradeClient      # 交易服务调用
│   ├── PayClient        # 支付服务调用
│   └── UserClient       # 用户服务调用
│
├── item-service/        # 商品服务 (端口 8081)
│   ├── 商品 CRUD / 分页 / 搜索
│   ├── MQ 消费：扣减库存
│   └── Feign 暴露：查商品详情、批量查商品
│
├── cart-service/        # 购物车服务 (端口 8082)
│   ├── 购物车增删改查
│   └── MQ 消费：下单后清理购物车
│
├── pay-service/         # 支付服务 (端口 8083)
│   ├── 支付单管理
│   └── MQ 发布：支付成功 → 通知交易服务
│
├── user-service/        # 用户服务 (端口 8084)
│   ├── 登录 / JWT 签发
│   └── 地址管理 / 余额扣减
│
├── trade-service/       # 交易/订单服务 (端口 8085)
│   ├── 订单创建、查询
│   ├── MQ 发布：订单创建事件
│   └── MQ 消费：支付成功标记 + 超时自动取消
│
└── hm-service/          # 单体聚合服务（旧版，含所有业务模块）
```

---

## 🔗 服务间通信

### 通信方式总览

```
                    ┌──────────────────────┐
                    │    Spring Cloud       │
                    │    Gateway (8080)     │
                    └──────┬───────┬───────┘
                           │       │
             ┌─────────────┘       └───────────--──┐
             ▼                                     ▼
      ┌─────────────┐                           ┌─────────────┐
      │   Nacos     │   服务注册发现            │   RabbitMQ   │
      │  注册中心    │◄──────────────——————────►│  消息队列    │
      └─────────────┘                           └──────┬──────┘
                                                       │
         ┌────── Feign 同步调用 ──────┐       ┌────── 异步消息 ────┐
         ▼                           ▼        ▼                    ▼
   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
   │ 商品服   │  │ 购物车服务│ │ 用户服务  │  │ 支付服务 │  │ 交易服务 │
   │ 8081     │  │ 8082     │  │ 8084     │  │ 8083     │  │ 8085     │
   └────┬─────┘  └──────────┘  └──────────┘  └────┬─────┘  └────┬─────┘
        │                                          │             │
        └──────────── Feign 调用 ──────────────────┘             │
             ▲                                ▲                  │
             │                                │                   │
             └───────── MQ 消息 ──────────────┴───────────────────┘
```

### 同步调用（Feign）— 查询 + 强一致性操作

| 调用方 | Feign 接口 | 用途 | 原因 |
|--------|-----------|------|------|
| **trade-service** → item-service | `ItemClient.queryItemByIds()` | 获取商品价格计算订单总额 | 需要实时价格 |
| **cart-service** → item-service | `ItemClient.queryItemByIds()` | 展示购物车商品名称、价格 | 用户查购物车需实时数据 |
| **pay-service** → user-service | `UserClient.deductMoney()` | 扣减用户余额 | 余额必须实时校验 |
| **gateway** → | 转发至各服务 | 所有外部请求入口 | 统一鉴权路由 |

### 异步消息（RabbitMQ）— 事件通知 + 最终一致性

| 发布者 | 路由键 | 消费者 | 操作 | 替换原 Feign |
|--------|--------|--------|------|-------------|
| **trade-service** | `order.created` | **item-service** | 扣减库存 | ✅ 替代 |
| **trade-service** | `order.created` | **cart-service** | 清理已购买商品 | ✅ 替代 |
| **trade-service** | `order.timeout` (DLQ 30min) | **trade-service** | 超时未支付自动取消订单 | ✨ 新增 |
| **pay-service** | `order.paid` | **trade-service** | 更新订单为已支付 | ✅ 替代 |

### 设计原则

```
查询 / 实时强一致     → Feign   同步   （必须等待结果）
命令 / 事件通知       → RabbitMQ 异步   （削峰填谷 + 解耦）
```

### 网关路由表

| 路由前缀 | 目标服务 |
|----------|----------|
| `/items/**`, `/search/**` | `item-service` |
| `/carts/**` | `cart-service` |
| `/users/**`, `/addresses/**` | `user-service` |
| `/orders/**` | `trade-service` |
| `/pay-orders/**` | `pay-service` |

### JWT 鉴权

网关通过 `AuthGlobalFilter` 全局过滤器拦截请求，解析 `authorization` 头中的 JWT Token（RSA 签名），将 `userId` 注入请求头传递给下游服务。白名单路径（登录、搜索、商品浏览等）放行。

---

## 🗄️ 数据库设计

每服务使用独立数据库，按业务边界拆分：

| 数据库 | 所属服务 | 主要表 |
|--------|----------|--------|
| `hm-item` | item-service | 商品表 |
| `hm-cart` | cart-service | 购物车表 |
| `hm-pay` | pay-service | 支付订单表 |
| `hm-user` | user-service | 用户表、地址表 |
| `hm-trade` | trade-service | 订单表、订单详情表、物流表 |

---

## 🚀 本地开发运行

### 前置条件

| 依赖 | 说明 |
|------|------|
| JDK 11+ | 项目编译运行 |
| MySQL 8.0 | 创建各业务数据库 |
| Nacos 2.x | 服务注册与配置中心 |
| RabbitMQ 3.x | 消息队列（可选，部分功能依赖） |
| Maven 3.6+ | 项目构建 |

### 启动步骤

**1. 启动 Nacos**（默认地址 `localhost:8848`）

**2. 启动 RabbitMQ**

```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management
```

管理控制台：http://localhost:15672（guest/guest）

**3. 初始化数据库**

```bash
# 创建数据库并导入数据（在 MySQL 客户端中执行）
mysql -u root -p < sql/hm-user.sql
mysql -u root -p < sql/hm-item.sql
mysql -u root -p < sql/hm-cart.sql
mysql -u root -p < sql/hm-trade.sql
mysql -u root -p < sql/hm-pay.sql

# Nacos 配置库（可选，如果使用独立 Nacos 需导入）
mysql -u root -p < sql/nacos.sql
```

> SQL 文件位于 `sql/` 目录下，按服务拆分，每个文件包含建库、建表和测试数据。

**4. 编译构建**

```bash
mvn clean compile
```

**5. 启动顺序**

```bash
# 先启动基础服务
cd user-service && mvn spring-boot:run    # 端口 8084
cd item-service && mvn spring-boot:run    # 端口 8081

# 再启动业务服务
cd cart-service && mvn spring-boot:run    # 端口 8082
cd trade-service && mvn spring-boot:run   # 端口 8085
cd pay-service && mvn spring-boot:run     # 端口 8083

# 最后启动网关
cd hm-gateway && mvn spring-boot:run      # 端口 8080
```

**6. 访问接口文档**

各服务独立暴露 Knife4j 接口文档，例如：
- 商品服务：`http://localhost:8081/doc.html`
- 用户服务：`http://localhost:8084/doc.html`
- 网关统一入口：`http://localhost:8080/doc.html`

---

## 🐳 Docker 部署

项目支持 Docker 容器化部署（参考 `hm-service/Dockerfile` 示例）：

```dockerfile
FROM openjdk:11.0-jre-buster
ENV TZ=Asia/Shanghai
COPY app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

各服务可独立打包镜像，结合 `docker-compose` 编排 Nacos + MySQL + RabbitMQ + 各微服务容器。

---

## 📨 RabbitMQ 消息队列

### 消息架构

```
┌───────────────┐     order.created     ┌────────────────┐
│               │──────────────────────▶│ item-service    │
│  trade-service │                      │  └─ 扣减库存    │
│  (订单服务)    │──┐                   └────────────────┘
│               │  ├── order.created   ┌────────────────┐
│               │  └──────────────────▶│ cart-service    │
│               │                      │  └─ 清购物车    │
└───────┬───────┘                      └────────────────┘
        │
        │ order.timeout (DLQ, 30min TTL)
        ▼
┌───────────────┐     order.paid       ┌────────────────┐
│  pay-service   │──────────────────────▶│ trade-service  │
│  (支付服务)    │                      │  └─ 标记已支付  │
└───────────────┘                      └────────────────┘
```

### 交换机与队列

| 交换机 | 类型 | 说明 |
|--------|------|------|
| `hmall.direct` | Direct | 业务主交换机 |

| 队列 | 路由键 | 消费者 | 说明 |
|------|--------|--------|------|
| `hmall.queue.order.created.item` | `order.created` | item-service | 扣减库存 |
| `hmall.queue.order.created.cart` | `order.created` | cart-service | 清理购物车 |
| `hmall.queue.order.paid` | `order.paid` | trade-service | 更新订单为已支付 |
| `hmall.queue.order.timeout.wait` | `order.timeout` | — | 超时等待（TTL 30min） |
| `hmall.queue.order.timeout.check` | `order.timeout.check` | trade-service | 超时取消订单 |

> 超时队列采用 **死信队列（DLQ）** 方案：消息先进入 `order.timeout.wait` 队列（无消费者，TTL=30min），到期后由 RabbitMQ 自动转发至 `order.timeout.check` 队列进行消费。

### 消息可靠性

- **事务提交后再发消息**：使用 `TransactionSynchronizationManager.registerSynchronization` 确保 DB 事务提交后才发送 MQ 消息
- **消费者手动确认**：默认 auto-ack，监听器抛出异常时消息重回队列
- **JSON 序列化**：全局配置 `Jackson2JsonMessageConverter`，收发自动序列化/反序列化

---

## 🧩 技术要点

- ✅ **统一异常处理** — `CommonExceptionAdvice` 全局拦截异常，统一返回格式
- ✅ **统一返回体** — `R<T>` 泛型封装，`code=200` 标识成功
- ✅ **Feign 接口隔离** — `hm-api` 模块定义全部 Feign 客户端，避免服务间直接耦合
- ✅ **混合通信架构** — 查询/强一致用 Feign 同步，事件通知用 RabbitMQ 异步解耦
- ✅ **JWT 登录态传递** — 网关解析 Token，请求头透传 `userId`
- ✅ **MyBatis-Plus 自动映射** — 枚举类型处理器、自动填充、乐观锁等
- ✅ **分页标准化** — `PageQuery` + `PageDTO` 统一分页参数与返回
- ✅ **RabbitMQ 异步消息** — 扣库存、清购物车、支付通知、超时取消全部解耦
- ✅ **延迟队列（DLQ）** — 死信队列实现 30 分钟订单超时自动取消，无需额外插件


