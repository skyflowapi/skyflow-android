# skyflow-android
---
[![CI](https://img.shields.io/static/v1?label=CI&message=passing&color=green?style=plastic&logo=github)](https://github.com/skyflowapi/skyflow-android/actions)
[![GitHub release](https://img.shields.io/github/v/release/skyflowapi/skyflow-android.svg)](https://github.com/skyflowapi/skyflow-android/releases)
[![License](https://img.shields.io/github/license/skyflowapi/skyflow-android)](https://github.com/skyflowapi/skyflow-android/blob/main/LICENSE)

Skyflow's Android SDKs let you securely collect, tokenize, and reveal sensitive data in your Android app without that data touching your front-end infrastructure. This repository ships **two independently-installable SDKs** built on a shared core:

| SDK | Vault contract | Gradle artifact | Guide |
|-----|----------------|-----------------|-------|
| **SkyVault** | v1 | `com.skyflowapi.android:skyflow-android-sdk` | [skyvault/README.md](skyvault/README.md) |
| **FlowVault** | v2 | `com.skyflowapi.android:skyflow-flowvault-android-sdk` | [flowvault/README.md](flowvault/README.md) |

## Which SDK do I need?

Install **one** of the two — they share the `Skyflow` package, so a single app uses whichever matches its vault:

- **SkyVault** — for a v1 (SkyVault) vault. This is the long-standing `skyflow-android-sdk`.
- **FlowVault** — for a v2 (FlowVault) vault.

If you're unsure which contract your vault uses, check with your Skyflow account team.

## Requirements

- Android 5.0 (API level 21) and above
- compileSdk 35 and above
- Android Gradle Plugin 8.6.0 and above

## Installation

Both SDKs are published to GitHub Packages. First configure your GitHub Personal Access Token as described in the [SkyVault](skyvault/README.md#configuration) or [FlowVault](flowvault/README.md#configuration) guide, then add the dependency for the SDK you need:

**SkyVault (v1)**

```groovy
implementation 'com.skyflowapi.android:skyflow-android-sdk:1.27.0'
```

**FlowVault (v2)**

```groovy
implementation 'com.skyflowapi.android:skyflow-flowvault-android-sdk:1.0.0'
```

See each guide for the latest published version.

## Documentation

- **SkyVault (v1):** [skyvault/README.md](skyvault/README.md)
- **FlowVault (v2):** [flowvault/README.md](flowvault/README.md)

## Samples

Runnable reference apps for each SDK live under [`samples/`](samples/README.md):

- [`samples/skyvault/`](samples/skyvault/) — SkyVault (v1) sample app
- [`samples/flowvault/`](samples/flowvault/) — FlowVault (v2) sample app

## License

See [LICENSE](LICENSE).
