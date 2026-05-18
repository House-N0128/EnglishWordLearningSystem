# 英语单词学习系统

南京航空航天大学 软件工程课程设计 · 第102组

一个完整的英语单词学习平台，包含 **后端API**、**Web前端** 和 **Android App** 三个模块。

## 项目结构

```
├── word-learning-backend/     # 后端 (Spring Boot + MyBatis + MySQL)
├── word-learning-web/         # Web前端 (HTML + CSS + JavaScript)
└── word-learning-app/         # Android App (Java原生)
```

## 功能概览

### 普通用户
- 注册 / 登录 / 找回密码 / 注销账号
- 浏览词书、选择词书开始学习
- 单词学习（单词卡片、翻页、音频播放）
- 收藏单词 / 取消收藏
- 查询单词详情
- 查看个人学习记录和学习统计
- 查看词书学习进度

### 管理员
- 管理用户账号（查询、冻结/解冻）
- 管理词书（添加、编辑、下架）
- 管理单词（添加、批量导入、编辑）
- 查看所有用户的学习统计数据

## 技术栈

| 模块 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2、MyBatis、MySQL、Lombok |
| Web前端 | 原生 HTML + CSS + JavaScript、Fetch API |
| Android | 原生 Java、OkHttp、Gson、Material Design |

## 快速开始

### 1. 数据库

创建 MySQL 数据库并导入数据：

```sql
CREATE DATABASE word_study DEFAULT CHARACTER SET utf8mb4;
```

然后修改 `word-learning-backend/src/main/resources/application.properties` 中的数据库连接信息。

### 2. 启动后端

```bash
cd word-learning-backend
./mvnw spring-boot:run
```

后端运行在 `http://localhost:8080`。

### 3. 启动 Web 前端

用任意 HTTP 服务器托管 `word-learning-web` 目录，例如：

```bash
cd word-learning-web
npx serve .
```

或者直接用浏览器打开 `word-learning-web/index.html`。

修改 `api.js` 中的 `BASE_URL` 指向你的后端地址。

### 4. 运行 Android App

用 Android Studio 打开 `word-learning-app` 目录，Sync Gradle 后点击 Run。

修改 `ApiClient.java` 中的 `BASE_URL` 指向你的后端地址。

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/login | 用户登录 |
| POST | /api/user/register | 用户注册 |
| GET | /api/user/profile | 获取个人信息 |
| PUT | /api/user/profile | 修改个人信息 |
| PUT | /api/user/password | 修改密码 |
| DELETE | /api/user/account | 注销账号 |
| POST | /api/admin/login | 管理员登录 |
| GET | /api/admin/stats | 管理统计 |
| GET | /api/wordbooks | 词书列表 |
| GET | /api/words?wordBookId= | 按词书查单词 |
| GET | /api/words/{id} | 单词详情 |
| GET | /api/words/search?keyword= | 搜索单词 |
| GET | /api/collections | 收藏列表 |
| POST | /api/collections/add | 收藏单词 |
| GET | /api/records/stats | 学习统计 |
| GET | /api/records/recent | 最近学习 |
| GET | /api/records/list | 学习记录 |
| POST | /api/records/add | 标记已学 |

## 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 普通用户 | 10001 | pwd_123456 |
| 管理员 | admin001 | admin_pwd_001 |

## 作者

- 周佳敏
- 王婧瑶
- 王品
- 侯森
