# 🏦 Bank Transaction Management System

一个基于 Spring Boot 和 Vue3 开发的简易银行交易管理系统，支持交易的创建、查询、更新和删除，并集成了限流、熔断、缓存等机制，可通过 Docker 一键部署前后端。

---

## 🚀 功能概览

- 💰 创建交易记录（支持基本信息校验）
- 📄 分页查询交易列表（按时间倒序）
- 🔁 更新交易
- ❌ 删除交易
- ⚡️ 接口限流 & 熔断（Resilience4j）
- 🧠 缓存查询结果
- 🔐 Swagger 接口文档（SpringDoc OpenAPI）
- 📦 前后端分离，支持 Docker Compose 快速部署

---

## 🛠️ 技术栈

| 后端 | 前端 |
|------|------|
| Java 21 + Spring Boot 3.2 | Vue 3 + Element Plus |
| Maven | Vite |
| Resilience4j | Axios |
| Lombok | -- |

---

## 📂 项目结构

```
bank-app/
├── backend/                    # 后端服务（Spring Boot）
│   ├── src/main/java/org/bank
│   │   ├── controller/         # 接口层
│   │   ├── service/impl/       # 业务实现
│   │   ├── model/              # 交易实体
│   │   └── BankTransactionApplication.java
│   ├── pom.xml                 # Maven 配置
│   └── Dockerfile
├── frontend/                   # （可选）前端工程
│   ├── src/                    # Vue / React 等前端代码
│   ├── Dockerfile
├── docker-compose.yml          # 一键部署配置
└── README.md                   # 项目说明

```

---

## 🐳 本地部署（Docker Compose）

### 1. 克隆项目

```
git clone git@github.com:your_username/bank-app.git
cd bank-app
```

2. 构建并启动服务
```
docker compose up --build -d
```
3. 访问服务
后端 API 接口（默认端口）：http://localhost:8080

Swagger 文档地址：http://localhost:8080/swagger-ui.html

前端页面地址：http://localhost

📌 API 示例
POST /transactions 创建交易

GET /transactions?page=1&size=10 分页获取交易

PUT /transactions/{id} 更新交易

DELETE /transactions/{id} 删除交易

⚙️ 自定义配置说明
后台配置（application.yml）中启用了：
```
resilience4j:
  rate-limiter:
    instances:
      transactionService:
        limit-for-period: 500000
        limit-refresh-period: 1s
        timeout-duration: 500ms
  circuitbreaker:
    instances:
      transactionService:
        sliding-window-size: 100
        failure-rate-threshold: 100
        wait-duration-in-open-state: 5s
        ignore-exceptions:
          - org.bank.common.exception.TransactionException
```

📦 本地开发（非 Docker）
后端启动
```
cd backend
./mvnw spring-boot:run
```

前端启动
```
cd frontend
npm install
npm run dev
```

🧪 单元测试
```
cd backend
./mvnw test
```

📄 License
本项目仅用于学习与技术交流，商业化使用请联系作者。
