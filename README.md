# skyflow-android
---
[![CI](https://img.shields.io/static/v1?label=CI&message=passing&color=green?style=plastic&logo=github)](https://github.com/skyflowapi/skyflow-android/actions)
[![GitHub release](https://img.shields.io/github/v/release/skyflowapi/skyflow-android.svg)](https://github.com/skyflowapi/skyflow-android/releases)
[![License](https://img.shields.io/github/license/skyflowapi/skyflow-android)](https://github.com/skyflowapi/skyflow-android/blob/main/LICENSE)

Skyflow's Android SDKs let you securely collect, tokenize, and reveal sensitive data in your Android app without that data touching your front-end infrastructure. This repository publishes **two SDKs from a shared codebase**. Pick the one that matches your vault type:

| SDK | Vault type | Install (Gradle) | Guide |
|-----|------------|------------------|-------|
| **skyflow-android-sdk** | PDB vault (v1 API) | `com.skyflowapi.android:skyflow-android-sdk:1.27.0` | [skyvault/README.md](skyvault/README.md) |
| **skyflow-flowvault-android-sdk** | Flow vault (v2 API) | `com.skyflowapi.android:skyflow-flowvault-android-sdk:1.0.0` | [flowvault/README.md](flowvault/README.md) |

If you are an existing Skyflow Android customer, stay on **skyflow-android-sdk** — it is fully backward compatible. **skyflow-flowvault-android-sdk** is a separate SDK (versioned from `1.0.0`) for Flow vaults, built on the v2 API. Both share the `Skyflow` package, so add whichever matches your vault.

## Requirements

- Android 5.0 (API level 21) and above
- compileSdk 35 and above
- Android Gradle Plugin 8.6.0 and above

## Installation

Both SDKs are published to GitHub Packages. First configure your GitHub Personal Access Token as described in the [skyflow-android-sdk](skyvault/README.md#configuration) or [skyflow-flowvault-android-sdk](flowvault/README.md#configuration) guide, then add the dependency for your vault type:

**PDB vault (v1 API)**

```groovy
implementation 'com.skyflowapi.android:skyflow-android-sdk:1.27.0'
```

**Flow vault (v2 API)**

```groovy
implementation 'com.skyflowapi.android:skyflow-flowvault-android-sdk:1.0.0'
```

See each guide for the latest published version.

## Documentation

- **skyflow-android-sdk** — PDB vault (v1 API): [skyvault/README.md](skyvault/README.md)
- **skyflow-flowvault-android-sdk** — Flow vault (v2 API): [flowvault/README.md](flowvault/README.md)

## Samples

Runnable reference apps for each SDK live under [`samples/`](samples/README.md):

- [`samples/skyvault/`](samples/skyvault/) — skyflow-android-sdk (PDB vault) sample
- [`samples/flowvault/`](samples/flowvault/) — skyflow-flowvault-android-sdk (Flow vault) sample

## License

See [LICENSE](LICENSE).
