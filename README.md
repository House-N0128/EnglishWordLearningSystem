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

## 环境要求

| 工具 | 版本 | 下载地址 |
|------|------|----------|
| JDK | 17+ | https://adoptium.net |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/mysql |
| Android Studio | 最新版 | https://developer.android.com/studio |
| Python | 3.x | https://www.python.org/downloads （Windows 安装时勾选 "Add Python to PATH"）|

## 克隆后完整搭建步骤

### 第一步：克隆项目

打开终端（PowerShell 或 CMD），找一个你放代码的目录：

```bash
git clone https://github.com/House-N0128/EnglishWordLearningSystem.git
cd EnglishWordLearningSystem
```

### 第二步：安装 MySQL 并导入数据库

1. 安装 MySQL 8.0+，记住你设置的 **root 密码**
2. 确认 MySQL 服务正在运行（Windows 在任务管理器 → 服务 里找 MySQL）

3. 打开终端，导入项目自带的数据库文件（含建表语句 + 测试数据）：

```bash
# 先创建空的数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS word_study DEFAULT CHARACTER SET utf8mb4;"

# 导入 SQL 文件（替换成你电脑上的实际路径）
mysql -u root -p word_study < word_study.sql
```

4. 打开 `word-learning-backend\src\main\resources\application.properties`，把数据库用户名和密码改成你自己的：

```properties
spring.datasource.username=你的MySQL用户名（通常是root）
spring.datasource.password=你的MySQL密码
```

### 第三步：启动后端

1. 确保你装了 **JDK 17+**（终端输入 `java -version` 验证）

2. 在项目根目录下：

```bash
cd word-learning-backend

# Windows 用户：
mvnw.cmd spring-boot:run

# Mac / Linux 用户：
./mvnw spring-boot:run
```

3. 看到 `Started WordLearningSystemApplication` 就表示成功了，后端运行在 **http://localhost:8080**

---

### 第四步：启动 Web 前端

Web 前端是纯 HTML 文件，需要一个简单的 HTTP 服务器来运行（不能直接双击打开，因为涉及跨域请求）。

**方式一（推荐，使用 Python）：**

```bash
cd word-learning-web
python -m http.server 3000
```

然后浏览器打开 **http://localhost:3000**

**方式二（使用 Node.js）：**

```bash
cd word-learning-web
npx serve .
```

**重要：** 如果后端不在同一台电脑上运行，需要修改前端代码里的后端地址：

- 桌面版：打开 `word-learning-web\api.js`，修改第 3 行的 `BASE_URL`
- 移动版：打开 `word-learning-web\mobile\api.js`，修改第 3 行的 `BASE_URL`

`BASE_URL` 默认是 `http://localhost:8080`，改成你的后端实际地址即可。

### 第五步：运行 Android App

1. **安装 Android Studio**（如果还没装），一路 Next 用默认设置即可

2. **用 Android Studio 打开项目：**
   - 启动 Android Studio
   - 点击 **Open** → 选择 `word-learning-app` 文件夹
   - 等待右下角进度条走完（第一次会比较慢，在下载 Gradle 和依赖库，可能需要 5-15 分钟）

3. **下载 Android SDK（如果还没装过）：**
   - 点击顶部工具栏 **SDK Manager** 图标（一个向下箭头 + 小盒子）
   - 在 **SDK Platforms** 标签页，勾选 **Android API 35**，点 Apply 下载
   - 在 **SDK Tools** 标签页，确认 **Android SDK Build-Tools** 已勾选

4. **Gradle Sync（小象图标）：**
   - 每次修改了 `build.gradle.kts` 后，点工具栏上的 **Sync Now**（蓝色小象 🐘）
   - 或者菜单 **File → Sync Project with Gradle Files**
   - 这个步骤会下载 OkHttp、Gson、Material Design 等第三方库

5. **修改后端地址：**
   打开 `app\src\main\java\com\example\wordlearningapp\api\ApiClient.java`，修改第 18 行的 `BASE_URL`：

   ```java
   // 如果用 Android 模拟器（模拟器和电脑在同一台机器）：
   private static final String BASE_URL = "http://10.0.2.2:8080";

   // 如果用真机测试（手机和电脑连同一个 WiFi）：
   private static final String BASE_URL = "http://你电脑的局域网IP:8080";
   ```
   > 查看电脑局域网 IP：Windows 终端输入 `ipconfig`，找 IPv4 地址（一般是 192.168.x.x）

6. **创建模拟器（如果没有真机）：**
   - 点右上角 **Device Manager**（手机图标）
   - 点 **Create device** → 选 Pixel 6 → 选 API 35 系统镜像（需要下载，等一会儿）→ Finish

7. **运行：**
   - 点顶部绿色 **▶ Run** 按钮
   - 选择你创建的模拟器
   - App 会自动安装并打开

---

## 常见问题

### 后端启动报错 "Access denied for user"
→ `application.properties` 里的数据库用户名或密码不对，改成你自己的。

### 后端启动报错 "Unknown database 'word_study'"
→ 说明没导入 SQL 文件，回到第二步执行 `mysql -u root -p < word_study.sql`。

### Android Studio 报 "Cannot find JAR 'kotlin-compiler-embeddable'"
→ 国内网络问题。打开 `settings.gradle.kts`，确认阿里云镜像在 `google()` 前面。还不行的话，关掉项目删掉 `C:\Users\你的用户名\.gradle\caches` 再重新打开。

### Android Studio 报 "Unknown unit 'dp'"
→ 布局文件写错了，联系项目维护者修复。

### 前端能打开但登录没反应
→ 打开浏览器 F12 → Console，看是否有 CORS 报错。如果有，说明后端没重启（CORS 配置没生效），重启后端即可。

### Web 前端直接用浏览器打开 HTML 文件能用吗？
→ 部分功能可以，但 API 调用涉及跨域，建议用 HTTP 服务器托管（第四步的方式一或方式二）。

### 模拟器里 App 连不上后端
→ 检查 `ApiClient.java` 里的 `BASE_URL`：
- 模拟器用 `http://10.0.2.2:8080`（这是 Android 模拟器访问宿主机的固定地址）
- 真机用电脑的局域网 IP

---

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

- 侯森
- 周佳敏
- 王婧瑶
- 王品

