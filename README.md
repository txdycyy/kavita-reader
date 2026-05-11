# Kavita Reader for Android

[![Android](https://github.com/txdycyy/kavita-reader/actions/workflows/android.yml/badge.svg)](https://github.com/txdycyy/kavita-reader/actions/workflows/android.yml)

Kavita Reader is a native Android EPUB reader for Kavita servers. It targets Android 8.0+ and focuses on the core reading flow: connect to a server, browse libraries, download EPUB books, and read offline with local progress and reader preferences.

Kavita Reader 是一个面向 Kavita 服务器的原生 Android EPUB 阅读器。首版目标是跑通最核心的阅读流程：连接服务器、浏览书库、下载 EPUB、离线阅读，并在本地保存阅读进度和阅读偏好。

## Features / 功能

- Native Android app built with Kotlin, Jetpack Compose, and Material 3
- Kavita REST authentication through the `x-api-key` header
- OPDS token fallback when a server exposes OPDS access but rejects REST Auth Key validation
- Server URL normalization with reverse proxy sub-path support
- Room cache for server info, libraries, books, downloads, reading progress, and reader settings
- Android Keystore-backed credential storage
- WorkManager downloads with retry support
- Offline EPUB parsing from app-private storage
- Reader settings for font size, font family, line height, margins, and color theme
- GitHub Actions CI and release APK workflow

- 使用 Kotlin、Jetpack Compose、Material 3 构建
- 支持 Kavita REST API 的 `x-api-key` 认证
- 当 REST Auth Key 不可用但 OPDS token 可用时，自动 fallback 到 OPDS
- 支持带子路径的服务器地址，例如反向代理后的 `/kavita`
- 使用 Room 缓存服务器、书库、书籍、下载、进度和阅读设置
- 使用 Android Keystore 加密保存凭据
- 使用 WorkManager 管理下载和失败重试
- EPUB 下载到 App 私有目录后可离线阅读
- 支持字号、字体、行距、页边距、浅色/深色/护眼主题
- GitHub Actions 自动构建并上传 debug APK

## Current Scope / 当前范围

This is an MVP. It is intentionally focused on EPUB reading for a single Kavita server and one local user profile.

这是一个 MVP 版本，当前只聚焦单服务器、单用户、本地 EPUB 阅读。

- Supported: EPUB browsing, download, offline reading, local progress, reader preferences
- Deferred: PDF reader, multi-server sync, Kavita progress write-back, signed release APK

- 已支持：EPUB 浏览、下载、离线阅读、本地进度、阅读设置
- 暂未支持：PDF 阅读、多服务器同步、回写 Kavita 阅读进度、签名 release APK

## Build / 构建

Install Android Studio or a compatible Android SDK, then run:

安装 Android Studio 或 Android SDK 后运行：

```bash
gradle :app:assembleDebug
```

Run unit tests:

运行单元测试：

```bash
gradle :app:testDebugUnitTest
```

If you add a Gradle wrapper later, use:

如果后续添加 Gradle Wrapper，可以改用：

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Install / 安装

Build a debug APK and install it with `adb`:

构建 debug APK 后通过 `adb` 安装：

```bash
gradle :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

You can also download the latest workflow artifact from GitHub Actions.

也可以在 GitHub Actions 的构建记录里下载最新的 APK artifact。

## Kavita Setup / Kavita 配置

Create or copy an API/Auth key from your Kavita user settings, then enter:

在 Kavita 用户设置中创建或复制 API/Auth Key，然后在 App 中填写：

- Server URL, for example `https://kavita.example.com` or `https://example.com/kavita`
- Auth Key/API Key or OPDS token

- 服务器地址，例如 `https://kavita.example.com` 或 `https://example.com/kavita`
- Auth Key/API Key，或 OPDS token

The app first validates the key through Kavita's REST API using the `x-api-key` header. If that fails, it tries the OPDS catalog at `/api/opds/{token}` and uses OPDS browsing/downloads for EPUB content.

App 会先通过 Kavita REST API 和 `x-api-key` header 校验。如果失败，会继续尝试 `/api/opds/{token}`；如果 OPDS 可用，就使用 OPDS 进行书库浏览和 EPUB 下载。

## GitHub Workflow / GitHub 流程

- Pull requests and pushes to `main` or `develop` run `gradle testDebugUnitTest assembleDebug`
- The debug APK is uploaded as a workflow artifact named `kavita-reader-debug-apk`
- Pushing a tag like `v0.1.0` creates a GitHub Release with `kavita-reader-debug.apk`

- PR、推送到 `main` 或 `develop` 会自动执行 `gradle testDebugUnitTest assembleDebug`
- debug APK 会作为 `kavita-reader-debug-apk` artifact 上传
- 推送 `v0.1.0` 这样的 tag 会自动创建 GitHub Release，并附带 `kavita-reader-debug.apk`

Create a release:

创建一个发布版本：

```bash
git tag v0.1.0
git push origin v0.1.0
```

## Security / 安全说明

- Do not commit Kavita API keys, OPDS tokens, server URLs, databases, or downloaded EPUB files
- Credentials are stored locally through Android Keystore-backed encryption
- Downloaded books are stored in app-private storage and are removed when the app is uninstalled

- 不要提交 Kavita API Key、OPDS token、服务器地址、数据库或下载的 EPUB 文件
- 凭据会通过 Android Keystore 加密保存在本机
- 下载的书籍保存在 App 私有目录，卸载 App 后会被系统删除

## Development Notes / 开发说明

- Minimum Android version: Android 8.0, API 26
- Main architecture: MVVM + Repository
- Network: Retrofit/OkHttp for REST, OkHttp/XML parsing for OPDS
- Local data: Room
- Background work: WorkManager
- Dependency injection: Hilt

- 最低 Android 版本：Android 8.0，API 26
- 主架构：MVVM + Repository
- 网络：REST 使用 Retrofit/OkHttp，OPDS 使用 OkHttp/XML 解析
- 本地数据：Room
- 后台任务：WorkManager
- 依赖注入：Hilt
