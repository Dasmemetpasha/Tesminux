# Tesminux

An experimental Android terminal tool built with **Rust**.

Tesminux aims to provide a lightweight, fast, and enjoyable terminal experience for Android while exploring Rust, Android development, and native system integration.

> 🚧 Tesminux is currently in **beta**. Features are still being developed and may change between releases.

## ✨ Current Status

**Version:** `0.1.0-beta.1`

Tesminux has reached its first Android beta release.

The current beta includes:

* Android application foundation
* Rust native core integration
* ARM64 Android support
* Custom Tesminux adaptive launcher icon
* Initial terminal application interface
* Android project and Gradle build system
* Real-device testing

**Tested on:** Redmi 12C
**Status:** Successfully tested ✅

## 🎯 Goals

* 🦀 Build the core with Rust
* 📱 Provide a reliable Android terminal experience
* ⚡ Keep Tesminux lightweight and responsive
* 💻 Support useful terminal functionality
* 🧪 Experiment with native Android and terminal features
* 📚 Learn and improve Rust, Android, and systems development

## 🛠️ Tech Stack

* **Rust** — Native core
* **Android** — Target platform
* **Kotlin** — Android application layer
* **Gradle** — Android build system
* **Cargo** — Rust build system and package manager

## 📦 Installation

Download the latest APK from the [Releases](https://github.com/Dasmemetpasha/Tesminux/releases) page.

For the current beta:

**`Tesminux-0.1.0-beta.1.apk`**

> ⚠️ Beta releases are intended for testing and development. Features may be incomplete or change in future versions.

## 🚀 Development

Clone the repository:

```bash
git clone https://github.com/Dasmemetpasha/Tesminux.git
cd Tesminux
```

### Rust Core

Build the Rust components with Cargo:

```bash
cargo build
```

Run the Rust project when applicable:

```bash
cargo run
```

### Android

Open the project in **Android Studio**, allow Gradle to synchronize, and build the Android application from the `app` module.

The Android application integrates the Tesminux Rust native core through the project's native library.

## 🗺️ Roadmap

The roadmap is flexible and will evolve as Tesminux develops.

* [x] Initial Rust core
* [x] Android project foundation
* [x] Rust native core integration
* [x] Android ARM64 support
* [x] Custom application icon
* [x] First Android beta
* [x] Real-device testing
* [ ] Improved terminal interface
* [ ] Command execution
* [ ] Terminal input/output improvements
* [ ] File system interaction
* [ ] Better error handling
* [ ] Performance improvements
* [ ] Additional Android architectures
* [ ] More terminal features
* [ ] Stable release

## 🤝 Contributing

Tesminux is an experimental open-source project.

Ideas, bug reports, testing, and contributions are welcome.

If you find a problem or have an idea for a feature, feel free to open an issue or contribute to the project.

## 📄 License

Tesminux is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.

See the [`LICENSE`](LICENSE) file for the full license text.

---

**Tesminux — A Rust-powered terminal experiment for Android.**
