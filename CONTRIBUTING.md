# Contributing

This project uses a simple GitHub flow.

## Development

1. Create a branch from `main`.
2. Keep changes scoped to one feature or fix.
3. Run `gradle testDebugUnitTest assembleDebug`.
4. Open a pull request and describe the tested workflow.

## Releases

Debug APK releases are created from tags.

```bash
git tag v0.1.0
git push origin v0.1.0
```

The `Release APK` workflow builds the APK, uploads it as a workflow artifact, and attaches it to a GitHub Release.

## Secrets

Do not commit Kavita API keys, OPDS tokens, server URLs, exported databases, or downloaded EPUB files.
