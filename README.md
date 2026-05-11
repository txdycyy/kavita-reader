# Kavita Reader for Android

[![Android](https://github.com/txdycyy/kavita-reader/actions/workflows/android.yml/badge.svg)](https://github.com/txdycyy/kavita-reader/actions/workflows/android.yml)

Native Android EPUB reader for Kavita servers. The MVP targets Android 8.0+ and focuses on connecting to one Kavita server, browsing libraries, downloading EPUB content, and reading offline with local progress and reading settings.

## Features

- Kotlin, Jetpack Compose, Material 3
- Kavita API key authentication with the `x-api-key` header
- OPDS token fallback for servers where the OPDS key is available but REST Auth Key validation fails
- Server URL normalization with sub-path support
- Room cache for server, libraries, series, downloads, progress, and reading settings
- Android Keystore-backed API key storage
- WorkManager full-series EPUB downloads
- Offline EPUB parsing and reading using app-private storage
- Reader settings for font size, line height, margins, font family, and color theme
- GitHub Actions build that uploads a debug APK

## Build

Install Android Studio or a compatible Android SDK, then run:

```bash
gradle :app:assembleDebug
```

If you add a Gradle wrapper, use:

```bash
./gradlew :app:assembleDebug
```

## GitHub Workflow

- Pull requests and pushes to `main` or `develop` run `gradle testDebugUnitTest assembleDebug`.
- The debug APK is uploaded as a workflow artifact named `kavita-reader-debug-apk`.
- Push a tag like `v0.1.0` to create a GitHub Release with `kavita-reader-debug.apk`.

## Kavita Setup

Create or copy an API/Auth key from your Kavita user settings, then enter:

- Server URL, for example `https://kavita.example.com` or `https://example.com/kavita`
- Auth Key/API Key

The app first validates the key through Kavita's authenticated API using the `x-api-key` header. If that fails, it tries the OPDS catalog at `/api/opds/{token}` and uses OPDS browsing/downloads for EPUB content.
