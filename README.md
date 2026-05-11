# Kavita Reader for Android

[![Android](https://github.com/txdycyy/kavita-reader/actions/workflows/android.yml/badge.svg)](https://github.com/txdycyy/kavita-reader/actions/workflows/android.yml)

[简体中文](README.zh-CN.md)

Kavita Reader is a native Android EPUB reader for [Kavita](https://www.kavitareader.com/) servers. It is designed for a focused reading workflow: connect to a Kavita instance, browse libraries, download EPUB books, and read offline with local progress and reader preferences.

This repository currently contains an MVP implementation. The first milestone prioritizes a working EPUB experience for one server and one local user profile.

## Highlights

- Native Android app built with Kotlin, Jetpack Compose, and Material 3
- Kavita REST authentication with the `x-api-key` header
- OPDS token fallback for servers where OPDS works but REST Auth Key validation fails
- Server URL normalization, including reverse proxy sub-paths
- Room cache for server metadata, libraries, books, downloads, reading progress, and reader settings
- Android Keystore-backed credential storage
- WorkManager download jobs with retry support
- Offline EPUB parsing from app-private storage
- Reader preferences for font size, font family, line height, margins, and theme
- GitHub Actions CI with debug APK artifacts and tag-based releases

## Scope

Supported in the MVP:

- Connect to one Kavita server
- Browse libraries and EPUB series/books
- Download EPUB content for offline use
- Open downloaded EPUB books in the built-in reader
- Save local reading progress
- Save global reader preferences

Not included yet:

- PDF reading
- Multi-server accounts
- Syncing reading progress back to Kavita
- Signed release APKs
- Full EPUB layout fidelity comparable to Readium-based readers

## Requirements

- Android 8.0 or newer, API 26+
- Android Studio or a compatible Android SDK
- Gradle installed locally, unless you build through GitHub Actions
- A Kavita Auth Key/API Key or an OPDS token

## Build

```bash
gradle :app:assembleDebug
```

Run unit tests:

```bash
gradle :app:testDebugUnitTest
```

If a Gradle wrapper is added later, use:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Install

Build and install the debug APK:

```bash
gradle :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

You can also download the latest `kavita-reader-debug-apk` artifact from the GitHub Actions page.

## Connect to Kavita

In Kavita, create or copy an Auth Key/API Key from your user settings. In the app, enter:

- Server URL, for example `https://kavita.example.com` or `https://example.com/kavita`
- Auth Key/API Key, or an OPDS token

The app first validates the key through Kavita's REST API using the `x-api-key` header. If that fails, it tries `/api/opds/{token}` and uses OPDS browsing/downloads when the OPDS catalog is available.

HTTP servers are supported for local or self-hosted Kavita instances, but HTTPS is recommended when the server is exposed outside your private network.

## GitHub Actions

The repository includes two workflows:

- `Android`: runs on pull requests, pushes to `main` or `develop`, and manual dispatch. It runs unit tests, builds a debug APK, and uploads it as an artifact.
- `Release APK`: runs on `v*` tags or manual dispatch. It builds the debug APK, uploads it as an artifact, and attaches it to a GitHub Release.

Create a release:

```bash
git tag v0.1.0
git push origin v0.1.0
```

## Architecture

- UI: Jetpack Compose and Material 3
- State: ViewModel, Kotlin Coroutines, Flow
- Persistence: Room
- Network: Retrofit/OkHttp for REST, OkHttp/XML parsing for OPDS
- Background work: WorkManager
- Dependency injection: Hilt
- Credential storage: Android Keystore-backed encryption

## Security Notes

- Do not commit Kavita API keys, OPDS tokens, server URLs, exported databases, or downloaded EPUB files.
- Credentials are stored locally with Android Keystore-backed encryption.
- Downloaded books are stored in app-private storage and are removed when the app is uninstalled.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the development flow and release notes.
