# Kavita Reader for Android

[![Android](https://github.com/txdycyy/kavita-reader/actions/workflows/android.yml/badge.svg)](https://github.com/txdycyy/kavita-reader/actions/workflows/android.yml)

[English](README.md)

Kavita Reader 是一个面向 [Kavita](https://www.kavitareader.com/) 服务器的原生 Android EPUB 阅读器。它聚焦最常用的阅读流程：连接 Kavita 实例、浏览书库、下载 EPUB，并在离线状态下继续阅读，同时保存本地阅读进度和阅读偏好。

当前仓库是 MVP 实现。第一阶段目标是先做出单服务器、单用户、本地 EPUB 阅读的可用版本。

## 功能亮点

- 使用 Kotlin、Jetpack Compose 和 Material 3 构建
- 支持 Kavita REST API 的 `x-api-key` 认证
- 当 OPDS 可用但 REST Auth Key 校验失败时，自动 fallback 到 OPDS token
- 服务器地址自动规范化，支持反向代理子路径
- 使用 Room 缓存服务器信息、书库、书籍、下载记录、阅读进度和阅读设置
- 使用 Android Keystore 加密保存凭据
- 使用 WorkManager 管理下载任务和失败重试
- EPUB 下载到 App 私有目录后可离线阅读
- 阅读器支持字号、字体、行距、页边距和主题设置
- GitHub Actions 自动构建、上传 debug APK，并支持 tag 发布

## 当前范围

MVP 已支持：

- 连接一个 Kavita 服务器
- 浏览书库和 EPUB 书籍/系列
- 下载 EPUB 到本地离线使用
- 使用内置阅读器打开已下载 EPUB
- 保存本地阅读进度
- 保存全局阅读偏好

暂未支持：

- PDF 阅读
- 多服务器账户
- 将阅读进度同步回 Kavita
- 签名 release APK
- 与 Readium 类阅读器同等级的 EPUB 排版还原

## 环境要求

- Android 8.0 或更高版本，API 26+
- Android Studio 或兼容的 Android SDK
- 本地安装 Gradle，或者使用 GitHub Actions 构建
- Kavita Auth Key/API Key，或 OPDS token

## 构建

```bash
gradle :app:assembleDebug
```

运行单元测试：

```bash
gradle :app:testDebugUnitTest
```

如果后续添加了 Gradle Wrapper，可以改用：

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## 安装

构建并安装 debug APK：

```bash
gradle :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

也可以到 GitHub Actions 页面下载最新的 `kavita-reader-debug-apk` artifact。

## 连接 Kavita

在 Kavita 用户设置里创建或复制 Auth Key/API Key。打开 App 后填写：

- 服务器地址，例如 `https://kavita.example.com` 或 `https://example.com/kavita`
- Auth Key/API Key，或 OPDS token

App 会先通过 Kavita REST API 和 `x-api-key` header 校验。如果失败，会继续尝试 `/api/opds/{token}`；如果 OPDS 目录可用，就使用 OPDS 浏览和下载 EPUB。

App 支持本地或自托管 Kavita 使用 HTTP 地址；如果服务器暴露到公网，建议使用 HTTPS。

## 在电脑上测试服务器

安装新 APK 之前，可以先在电脑上用同一组服务器地址和 token 做诊断：

```bash
KAVITA_URL="http://host:5051/" KAVITA_TOKEN="your-key-or-opds-token" scripts/kavita_probe.sh
```

如果要查看某个 OPDS 书库：

```bash
KAVITA_URL="http://host:5051/" KAVITA_TOKEN="your-token" KAVITA_LIBRARY_ID=10 scripts/kavita_probe.sh
```

脚本会告诉你当前凭据应该走 REST Auth Key 模式，还是 OPDS fallback 模式。

## GitHub Actions

仓库包含两个 workflow：

- `Android`：在 PR、推送到 `main` 或 `develop`、手动触发时运行。它会执行单元测试、构建 debug APK，并上传 artifact。
- `Release APK`：在推送 `v*` tag 或手动触发时运行。它会构建 debug APK、上传 artifact，并把 APK 附加到 GitHub Release。

创建发布版本：

```bash
git tag v0.1.0
git push origin v0.1.0
```

## 架构

- UI：Jetpack Compose 和 Material 3
- 状态管理：ViewModel、Kotlin Coroutines、Flow
- 本地存储：Room
- 网络：REST 使用 Retrofit/OkHttp，OPDS 使用 OkHttp/XML 解析
- 后台任务：WorkManager
- 依赖注入：Hilt
- 凭据存储：Android Keystore 加密

## 安全说明

- 不要提交 Kavita API Key、OPDS token、服务器地址、导出的数据库或下载的 EPUB 文件。
- 凭据会通过 Android Keystore 加密保存在本机。
- 下载的书籍保存在 App 私有目录，卸载 App 后会被系统删除。

## 贡献

开发流程和发布说明见 [CONTRIBUTING.md](CONTRIBUTING.md)。
