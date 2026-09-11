<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="150" alt="Hyper Island Logo" style="border-radius: 20%;" />
</p>

<h1 align="center">Hyper Island</h1>

<p align="center">
  <strong>Bring the native HyperIsland experience to third-party apps on HyperOS.</strong>
</p>

<p align="center">
  Hyper Island bridges standard Android notifications into the pill-shaped UI around the camera cutout, for a seamless, iOS-like experience on Xiaomi phones — with full theme customization and widget support.
</p>

<p align="center">
  <em>Not yet on the Play Store — build from source, see Installation below.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white" alt="Material Design" />
</p>

<p align="center">
  <sub>A fork of <a href="https://github.com/D4vidDf/HyperBridge">HyperBridge</a> by <a href="https://github.com/D4vidDf">D4vidDf</a>, licensed Apache 2.0. See the License &amp; Attribution section below.</sub>
</p>

<br>

## <img src="https://api.iconify.design/lucide:rocket.svg?color=%23808080" width="20" height="20" align="absmiddle" /> Features

* **Native visuals** — Turns standard notifications into HyperOS-style islands.
* **Theme engine** — Full control over how islands look.
    * **Theme creator** — Built-in editor with real-time previews.
    * **Smart colors** — Pulls accent colors from app icons automatically.
    * **Icon shaping** — Squircle, Clover, Arch, or Cookie.
    * **Granular control** — Per-app overrides for colors, icons, and action styles.
    * **Smart icon tinting** — Keeps dark or monochrome icons visible.
* **Widgets** — Pin standard Android widgets to the island layer, including the lockscreen.
* **Intelligent permanent island** — Hides itself automatically when a widget is active, so the two never overlap.
* **Smart integration**
    * **Media** — Album art and "Now Playing" status, with visualizer support.
    * **Navigation** — Real-time turn-by-turn for Google Maps and Waze.
    * **Downloads** — Circular progress layout with a completion animation.
    * **Calls** — Dedicated layout for incoming and active calls, with timers.
* **Spoiler protection** — Block specific terms globally or per app so they never show on the island.
* **Sui & Shizuku support** — Enhanced integration on rooted devices.
* **Total control** — Choose which apps trigger the island, set timeouts, and toggle floating behavior per app.

## <img src="https://api.iconify.design/lucide:code-2.svg?color=%23808080" width="20" height="20" align="absmiddle" /> For Developers: Create Themes

Hyper Island supports an open theming standard (`.hbr` packages). Build themes and distribute them, or wire an "Apply Theme" button directly into your own app (launcher, icon pack, etc.).

* **Documentation:** [Full guide on creating & distributing themes](https://github.com/nerufuyo/hyper-island/discussions/78)
* **Intent API:** Send themes programmatically using `com.nerufuyo.hyperisland.APPLY_THEME`.

## <img src="https://api.iconify.design/lucide:languages.svg?color=%23808080" width="20" height="20" align="absmiddle" /> Supported Languages

Translations below were carried over from the upstream HyperBridge project (see attribution). Want to help translate Hyper Island going forward? Open an issue or PR with new/updated `strings.xml` files.

* 🇺🇸 **English** (Default)
* 🇪🇸 **Spanish** (Español)
* 🇧🇷 **Portuguese** (Português Brasileiro) — Thanks to [@NIICKTCHUNS](https://github.com/NIICKTCHUNS)
* 🇵🇱 **Polish** (Polski) — Thanks to [@kacskrz](https://github.com/kacskrz)
* 🇸🇰 **Slovak** (Slovenčina)
* 🇰🇷 **Korean** (한국어) — Thanks to [@alexkoala](https://github.com/alexkoala)
* 🇺🇦 **Ukrainian** (Українська) — Thanks to [@ItzDFPlayer](https://github.com/ItzDFPlayer)
* 🇷🇺 **Russian** (Русский) — Thanks to [@kilo3528](https://github.com/kilo3528)
* 🇩🇪 **German** (Deutsch) — Thanks to [@kilo3528](https://github.com/kilo3528)
* 🇮🇩 **Indonesian** (Bahasa Indonesia)
* 🇹🇷 **Turkish** (Türkçe)

## <img src="https://api.iconify.design/lucide:wrench.svg?color=%23808080" width="20" height="20" align="absmiddle" /> Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3 Expressive)
* **Architecture:** MVVM
* **Storage:** Room Database (SQLite)
* **Services:** NotificationListenerService, WidgetOverlayService
* **Concurrency:** Kotlin Coroutines & Flow

## <img src="https://api.iconify.design/lucide:image.svg?color=%23808080" width="20" height="20" align="absmiddle" /> Screenshots

| Home Screen | Active Island | Theme Creator | Widget Picker |
|:---:|:---:|:---:|:---:|
| ![Home](./screenshots/home.png) | ![Island](./screenshots/island_example.png) | ![Creator](./screenshots/theme_creator.png) | ![Widgets](./screenshots/widget_picker.png) |

> Screenshots still show the upstream HyperBridge branding — pending a refresh once the app icon and in-app assets are updated.

## <img src="https://api.iconify.design/lucide:handshake.svg?color=%23808080" width="20" height="20" align="absmiddle" /> Acknowledgements

Thanks to the following people and projects for their contributions:

* **[Stardawn](https://www.coolapk1s.com/feed/70418983)** — for the research on the XMSF notification workaround that enables HyperIslands on Chinese ROMs.

## <img src="https://api.iconify.design/lucide:download.svg?color=%23808080" width="20" height="20" align="absmiddle" /> Installation

Not yet published to the Play Store.

1.  Download the latest APK from the [Releases](https://github.com/nerufuyo/hyper-island/releases) page (once available), or build from source in Android Studio.
2.  Install the APK on your Xiaomi/POCO/Redmi device.

### Setup

1.  Grant **Notification Access** when prompted.
2.  **Critical:** Follow the in-app guide to enable **Autostart** and **No Restrictions** (Battery) — otherwise the system will kill the background service.

## <img src="https://api.iconify.design/lucide:git-pull-request.svg?color=%23808080" width="20" height="20" align="absmiddle" /> Contributing

Contributions are welcome. Read the [Contributing Guidelines](CONTRIBUTING.md) before opening a Pull Request.

1.  **Fork** the repository.
2.  Create a branch (`git checkout -b feature/amazing-feature`).
3.  Commit your changes (`git commit -m 'Add amazing feature'`).
4.  Push the branch (`git push origin feature/amazing-feature`).
5.  Open a **Pull Request**.

## <img src="https://api.iconify.design/lucide:heart.svg?color=%23808080" width="20" height="20" align="absmiddle" /> Support the Project

Hyper Island is open source, built in spare time. If it's improved your daily experience, consider supporting its development.

<a href="https://github.com/sponsors/nerufuyo">
  <img src="https://img.shields.io/static/v1?label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86" width="150" alt="Sponsor"/>
</a>

## <img src="https://api.iconify.design/lucide:scale.svg?color=%23808080" width="20" height="20" align="absmiddle" /> License & Attribution

Distributed under the Apache 2.0 License. See `LICENSE` for details.

**Hyper Island is a fork of [HyperBridge](https://github.com/D4vidDf/HyperBridge) by [D4vidDf](https://github.com/D4vidDf)**, also Apache 2.0 licensed. Full credit to the original author for the core notification-bridging engine, theme system, and translator architecture this project builds on. See [NOTICE](NOTICE) for details.

## <img src="https://api.iconify.design/lucide:user.svg?color=%23808080" width="20" height="20" align="absmiddle" /> Developer

**nerufuyo**
* Website: [github.com/nerufuyo](https://github.com/nerufuyo)
* GitHub: [@nerufuyo](https://github.com/nerufuyo)
