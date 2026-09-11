<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="150" alt="HyperIsland Logo" style="border-radius: 20%;" />
</p>

<h1 align="center">Hyper Island</h1>

<p align="center">
  <strong>Bring the native HyperIsland experience to third-party apps on HyperOS.</strong>
</p>

<p align="center">
  Hyper Island bridges standard Android notifications into the pill-shaped UI around the camera cutout, offering a seamless, iOS-like experience on Xiaomi phones. Now with full theme customization and widget support.
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
  <sub>A fork of <a href="https://github.com/D4vidDf/HyperBridge">HyperBridge</a> by <a href="https://github.com/D4vidDf">D4vidDf</a>, licensed Apache 2.0. See <a href="#-license--attribution">License & Attribution</a>.</sub>
</p>

<br>

## 🚀 Features

* **Native Visuals:** Transforms notifications into HyperOS system-style islands.
* **🎨 Theme Engine:** Customize every pixel.
    * **Theme Creator:** Built-in editor to design your own themes with real-time previews.
    * **Smart Colors:** Automatically extract vibrant brand colors from app icons.
    * **Icon Shaping:** Choose between shapes like *Squircle*, *Clover*, *Arch*, and *Cookie*.
    * **Granular Control:** Per-app overrides for colors, icons, and action styles.
    * **Smart Icon Tinting:** Intelligently tints dark/monochrome icons to remain visible.
* **🧩 Widgets:** Pin standard Android widgets to the island layer for quick access—even on the Lockscreen!
* **🧠 Intelligent Permanent Island:** The permanent island now automatically hides itself whenever an active widget is shown on the screen, preventing awkward overlaps.
* **Smart Integration:**
    * **🎵 Media:** Show album art and "Now Playing" status with visualizer support.
    * **🧭 Navigation:** Real-time turn-by-turn instructions (Google Maps, Waze).
    * **⬇️ Downloads:** Dedicated circular progress layout with a satisfying "Green Tick" animation upon completion.
    * **📞 Calls:** Dedicated layout for incoming and active calls with timers.
* **🛡️ Spoiler Protection:** Define blocked terms globally or per-app to prevent specific notifications (e.g., message spoilers) from popping up on the Island.
* **Sui & Shizuku Support:** Fully supports Sui and Shizuku for enhanced network operations and seamless integration on rooted devices.
* **Total Control:** Choose exactly which apps trigger the island, customize timeouts, and toggle floating behavior per app.

## 👩‍💻 For Developers: Create Themes

HyperIsland supports an open theming standard (`.hbr` packages). You can create themes and distribute them, or integrate a "Apply Theme" button directly into your own app (Launcher, Icon Pack, etc.).

* **Documentation:** [Full Guide on Creating & Distributing Themes](https://github.com/nerufuyo/hyper-island/discussions/78)
* **Intent API:** Send themes programmatically using `com.nerufuyo.hyperisland.APPLY_THEME`.

## 🌐 Supported Languages

Translations below were carried over from the upstream HyperBridge project (see attribution). Want to help translate HyperIsland going forward? Open an issue or PR with new/updated `strings.xml` files.

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

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3 Expressive)
* **Architecture:** MVVM
* **Storage:** Room Database (SQLite)
* **Services:** NotificationListenerService, WidgetOverlayService
* **Concurrency:** Kotlin Coroutines & Flow

## 📸 Screenshots

| Home Screen | Active Island | Theme Creator | Widget Picker |
|:---:|:---:|:---:|:---:|
| ![Home](./screenshots/home.png) | ![Island](./screenshots/island_example.png) | ![Creator](./screenshots/theme_creator.png) | ![Widgets](./screenshots/widget_picker.png) |

## 🤝 Acknowledgements

Special thanks to the following people and projects for their invaluable contributions:

*   **[Stardawn](https://www.coolapk1s.com/feed/70418983)**: For the extensive research on the XMSF notification workaround that enables HyperIslands on Chinese ROMs.

## 📥 Installation

Not yet published to the Play Store.

1.  Download the latest APK from the [Releases](https://github.com/nerufuyo/hyper-island/releases) page (once available), or build from source in Android Studio.
2.  Install the APK on your Xiaomi/POCO/Redmi device.

### ⚙️ Setup (Required for both methods)
1.  Grant **"Notification Access"** when prompted.
2.  **Critical:** Follow the in-app guide to enable **Autostart** and **No Restrictions** (Battery) to prevent the system from killing the background service.

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting a Pull Request.

1.  **Fork** the repository.
2.  Create a new branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a **Pull Request**.

## 💖 Support the Project

Hyper Island is an open-source project developed in my free time. If this app has improved your daily experience, please consider supporting its development!

<a href="https://github.com/sponsors/nerufuyo">
  <img src="https://img.shields.io/static/v1?label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86" width="150" alt="Sponsor"/>
</a>

## 📜 License & Attribution

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

**Hyper Island is a fork of [HyperBridge](https://github.com/D4vidDf/HyperBridge) by [D4vidDf](https://github.com/D4vidDf)**, also Apache 2.0 licensed. Full credit to the original author for the core notification-bridging engine, theme system, and translator architecture this project builds on. See [NOTICE](NOTICE) for details.

## 👤 Developer

**nerufuyo**
* Website: [github.com/nerufuyo](https://github.com/nerufuyo)
* GitHub: [@nerufuyo](https://github.com/nerufuyo)
