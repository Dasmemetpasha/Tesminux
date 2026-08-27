# 🦀 Tesminux

**Tesminux**, Android için geliştirilmiş, Rust tabanlı hafif bir terminal uygulamasıdır.

Tesminux'un terminal altyapısı **Rust** ile, Android kullanıcı arayüzü ise **Kotlin + Jetpack Compose** ile geliştirilmiştir.

> 🏁 **Tesminux V10 — FINAL RELEASE**
>
> Tesminux'un aktif geliştirmesi sona ermiştir. **V10, projenin son resmi sürümüdür.**

---

## 🏁 Proje Durumu

**Development: Discontinued**

Tesminux artık aktif olarak geliştirilmiyor.

V10 sürümüyle birlikte:

* Yeni özellik geliştirmeleri durdurulmuştur.
* Yeni sürüm yayınlanması planlanmamaktadır.
* Aktif bakım sona ermiştir.
* Repository kaynak kodu arşiv amacıyla açık tutulmaktadır.

Mevcut V10 sürümü kullanılabilir durumda bırakılmıştır.

> Proje daha sonra yeniden geliştirilmeye başlanırsa bu durum ayrıca duyurulacaktır.

---

# ✨ Tesminux V10

V10, Tesminux'un geliştirme sürecindeki son sürümüdür.

Bu sürümde Android uygulaması ve Rust native core üzerinde önemli değişiklikler yapılmıştır.

### Android

* Kotlin tabanlı Android uygulaması
* Jetpack Compose arayüzü
* Terminal kullanıcı arayüzü
* Android sistem entegrasyonu
* JNI üzerinden native Rust core bağlantısı

### Rust Core

Tesminux'un native terminal altyapısı Rust ile geliştirilmiştir.

```text
tesminux-core/
├── src/
│   ├── ansi.rs
│   ├── commands.rs
│   ├── filesystem.rs
│   ├── lib.rs
│   ├── main.rs
│   └── terminal/
│       ├── history.rs
│       ├── mod.rs
│       ├── pty.rs
│       └── session.rs
├── Cargo.toml
└── Cargo.lock
```

---

# 🏗️ Mimari

Tesminux iki ana katmandan oluşur:

```text
┌──────────────────────────────────────┐
│              Android UI              │
│          Kotlin + Compose            │
└──────────────────┬───────────────────┘
                   │
                   │ JNI
                   ▼
┌──────────────────────────────────────┐
│             Tesminux Core            │
│                 Rust                 │
├──────────────────────────────────────┤
│ PTY                                  │
│ Terminal Sessions                    │
│ ANSI Processing                      │
│ Commands                             │
│ Filesystem                           │
│ History                              │
└──────────────────────────────────────┘
```

### Teknolojiler

| Teknoloji          | Kullanım                   |
| ------------------ | -------------------------- |
| 🦀 Rust            | Native terminal core       |
| 📱 Kotlin          | Android uygulaması         |
| 🎨 Jetpack Compose | Kullanıcı arayüzü          |
| 🔗 JNI             | Kotlin ↔ Rust iletişimi    |
| ⚙️ Cargo           | Rust build sistemi         |
| 🏗️ Gradle         | Android build sistemi      |
| 🖥️ PTY            | Terminal process altyapısı |

---

# 📱 Sistem Gereksinimleri

Tesminux:

**Android 7.0 (API 24)+**

için geliştirilmiştir.

Önerilen ortam:

* Android 7.0 veya üzeri
* ARM64 Android cihaz
* Yeterli depolama alanı

---

# 📦 Kurulum

Tesminux'un son sürümü GitHub Releases üzerinden APK olarak kullanılabilir.

Kaynak koddan derlemek isteyen kullanıcılar repository'yi klonlayabilir.

```bash
git clone https://github.com/Dasmemetpasha/Tesminux.git
cd Tesminux
```

---

# 🛠️ Kaynak Koddan Derleme

## Rust Core

```bash
cd tesminux-core
cargo check
```

Android native build için Android NDK ve gerekli Rust Android target'larının kurulmuş olması gerekir.

ARM64 Android target:

```text
aarch64-linux-android
```

---

## Android

Repository'yi Android Studio ile açabilirsiniz.

Windows:

```powershell
.\gradlew assembleDebug
```

Linux:

```bash
./gradlew assembleDebug
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

# 📁 Proje Yapısı

```text
Tesminux/
│
├── app/
│   └── src/
│       ├── androidTest/
│       └── main/
│           ├── java/
│           │   └── com/tesminux/app/
│           │       ├── MainActivity.kt
│           │       └── ui/
│           │
│           ├── jniLibs/
│           │   └── arm64-v8a/
│           │       └── libtesminux_core.so
│           │
│           ├── res/
│           └── AndroidManifest.xml
│
├── tesminux-core/
│   ├── src/
│   │   ├── ansi.rs
│   │   ├── commands.rs
│   │   ├── filesystem.rs
│   │   ├── lib.rs
│   │   ├── main.rs
│   │   └── terminal/
│   │       ├── history.rs
│   │       ├── mod.rs
│   │       ├── pty.rs
│   │       └── session.rs
│   │
│   ├── Cargo.toml
│   └── Cargo.lock
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── LICENSE
├── README.md
└── ai_guidelines.md
```

---

# 🔐 Güvenlik

Tesminux bir terminal uygulaması olduğundan çalıştırılan komutların etkileri kullanılan Android ortamına ve shell'e bağlıdır.

Tesminux'un native terminal altyapısı Rust ile oluşturulmuştur.

Kullanıcılar terminal üzerinden çalıştırdıkları komutların ne yaptığını kontrol etmelidir.

---

# 🐛 Hata Bildirme

Proje artık aktif olarak geliştirilmediği için yeni hataların düzeltilmesi veya özellik isteklerinin uygulanması garanti edilmez.

Bununla birlikte repository açık olduğundan kullanıcılar:

* Hata bildirebilir
* Kaynak kodu inceleyebilir
* Kendi fork'larını oluşturabilir
* Kendi geliştirmelerini sürdürebilir

---

# 🤝 Fork ve Devam Ettirme

Tesminux'un geliştirmesini devam ettirmek isteyen herkes repository'yi fork ederek kendi sürümünü oluşturabilir.

Örneğin:

```bash
git clone https://github.com/Dasmemetpasha/Tesminux.git
cd Tesminux
```

Daha sonra kendi branch'inizi oluşturabilirsiniz:

```bash
git checkout -b my-tesminux
```

Tesminux'un kaynak kodu, projenin geliştirme sürecini incelemek ve yeni projelere temel oluşturmak için kullanılabilir.

---

# 📜 Lisans

Lisans bilgileri repository içerisindeki [`LICENSE`](LICENSE) dosyasında bulunmaktadır.

---

# 🕊️ Son Söz

Tesminux bir öğrenme ve geliştirme projesi olarak başladı.

Android üzerinde Rust kullanarak bir terminal altyapısı oluşturmak, JNI ile native kodu Android'e bağlamak ve zaman içerisinde uygulamayı geliştirmek projenin temel amaçları arasındaydı.

**V10 ile birlikte bu yolculuk sona eriyor.**

Projeyi kullanan, test eden, hata bildiren, fikir veren veya kaynak koduna katkıda bulunan herkese teşekkürler. ❤️

---

## 🏁 Tesminux V10

**Final Release**

> Development discontinued.
> The repository remains available for archival and educational purposes.

🦀 **Rust Core** · 📱 **Android** · 🏁 **V10 Final**
