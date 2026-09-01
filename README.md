# 优购商城（YouGou Mall）

完整的电商系统 + 秒杀模块，前后端分离架构。

## 项目简介

优购商城是一个前后端分离的电商平台，包含用户端商城、商家后台、平台管理后台，
并集成了高并发秒杀模块。支持多角色、优惠券、商品评价、收藏等完整电商业务流程。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 + TypeScript + Vite + Pinia + Element Plus |
| 后端 | Spring Boot 4 + MyBatis-Plus + MySQL 8 + Redis |
| 安全 | BCrypt + JWT + 拦截器 |
| 测试 | JUnit 5 + Mockito |
| 文档 | SpringDoc OpenAPI（Swagger UI） |

## 角色与权限

| 角色 | 说明 |
|------|------|
| 用户（买家） | 浏览、搜索、购物车、下单、支付、优惠券、评价、收藏 |
| 商家（卖家） | 商品管理、库存管理、订单处理 |
| 系统管理员 | 用户/商家管理、分类管理、秒杀活动配置、全局订单管理 |

## 核心功能

### 用户端
- 注册登录（JWT 鉴权）
- 商品列表 / 搜索联想 / 商品详情（SKU 多规格）
- 购物车（全选、批量删除）
- 下单（优惠券抵扣、防超卖）
- 模拟支付、订单管理
- 商品评价、收藏

### 秒杀模块
- Redis 缓存库存 + 原子扣减
- 数据库兜底防超卖
- 限购控制
- 倒计时展示

### 商家后台
- 商品 CRUD（图片上传）
- 库存管理
- 订单查看与处理

### 管理员后台
- 用户 / 商家审核
- 分类管理
- 秒杀活动配置
- 优惠券模板管理
- 全局订单管理

## 快速开始

### 环境要求
- JDK 25+
- MySQL 8.0+
- Redis
- Node.js 18+ / pnpm

### 后端启动

```bash
cd server
mvn spring-boot:run
```

### 前端启动

```bash
cd client
pnpm install
pnpm dev
```

### 默认账号

| 账号 | 密码 | 角色 |
|------|------|------|
| buyer001 | 123456 | 买家 |
| seller001 | 123456 | 卖家 |
| admin001 | 123456 | 管理员 |

## API 文档

启动后访问：http://localhost:8080/swagger-ui/index.html

## 目录结构

```
├── client/          # 前端（Vue 3 + Vite）
├── server/          # 后端（Spring Boot 4）
├── docs/            # 设计文档
└── README.md
```
