# HMall — 飞猪电商微服务项目

基于 **Spring Cloud Alibaba** 的电商微服务实战项目，拆分多个独立子服务，涵盖商品、购物车、订单、支付、用户等核心业务模块。

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
| 服务调用 | OpenFeign（OkHttp 实现） |
| 接口文档 | Knife4j (Swagger) |

---

## 🏗️ 模块结构

```
hmall/
├── hm-gateway/          # 统一网关 (端口 8080)
│   └── 路由转发、JWT 登录校验、鉴权拦截
│
├── hm-common/           # 公共模块（工具类、异常、统一返回体）
│   ├── 统一返回体 R<T>
│   ├── 全局异常处理 CommonExceptionAdvice
│   ├── 分页查询 PageQuery / PageDTO
│   └── 工具类 BeanUtils、CollUtils、UserContext 等
│
├── hm-api/              # Feign 接口定义（服务间调用 API）
│   ├── ItemClient       # 商品服务调用
│   ├── CartClient       # 购物车服务调用
│   ├── TradeClient      # 交易服务调用
│   ├── PayClient        # 支付服务调用
│   └── UserClient       # 用户服务调用
│
├── item-service/        # 商品服务 (端口 8081)
│   ├── 商品 CRUD
│   ├── 分页查询 / 搜索
│   └── 库存扣减
│
├── cart-service/        # 购物车服务 (端口 8082)
│   ├── 购物车增删改查
│   └── 购物车商品管理
│
├── pay-service/         # 支付服务 (端口 8083)
│   ├── 支付单管理
│   └── 多渠道支付对接
│
├── user-service/        # 用户服务 (端口 8084)
│   ├── 登录 / JWT 签发
│   └── 地址管理
│
├── trade-service/       # 交易/订单服务 (端口 8085)
│   ├── 订单创建、查询
│   └── 订单详情 / 物流
│
└── hm-service/          # 单体聚合服务（旧版，含所有业务模块）
```

---

## 🔗 服务间通信

- **服务发现**：Nacos（各服务启动时注册到 `localhost:8848`）
- **服务调用**：OpenFeign + OkHttp（声明式 HTTP 客户端）
- **统一入口**：Spring Cloud Gateway（端口 8080）

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
| Maven 3.6+ | 项目构建 |

### 启动步骤

**1. 启动 Nacos**（默认地址 `localhost:8848`）

**2. 创建数据库** — 按上述数据库设计创建各库

**3. 编译构建**

```bash
mvn clean compile
```

**4. 启动顺序**

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

**5. 访问接口文档**

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

各服务可独立打包镜像，结合 `docker-compose` 编排 Nacos + MySQL + 各微服务容器。

---

## 🧩 技术要点

- ✅ **统一异常处理** — `CommonExceptionAdvice` 全局拦截异常，统一返回格式
- ✅ **统一返回体** — `R<T>` 泛型封装，`code=200` 标识成功
- ✅ **Feign 接口隔离** — `hm-api` 模块定义全部 Feign 客户端，避免服务间直接耦合
- ✅ **JWT 登录态传递** — 网关解析 Token，请求头透传 `userId`
- ✅ **MyBatis-Plus 自动映射** — 枚举类型处理器、自动填充、乐观锁等
- ✅ **分页标准化** — `PageQuery` + `PageDTO` 统一分页参数与返回

---
