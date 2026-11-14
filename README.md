# Garda Hijau Mengger

Aplikasi Android untuk pengelola sampah dan peternak maggot BSF (*Black Soldier Fly*) di Kelurahan Mengger. Dengan aplikasi ini pengelola bisa mencatat sampah organik yang masuk, memantau siklus maggot dari telur sampai panen, melihat perkiraan hasil panen, dan bertanya ke asisten GardaBot.

Dibuat untuk program **BEM Mahasiswa Berdampak 2026 — Sinergi Garda Hijau Mengger**. Backend: `https://gardahijaukemdiktisaintek.digital` ([dokumentasi API](https://gardahijaukemdiktisaintek.digital/api-docs)).

> **Status:** dalam pengembangan. Build debug memakai server asli; build `demo` memakai [data contoh](#mode-demo) tanpa server.

## Daftar isi

- [Fitur](#fitur)
- [Teknologi](#teknologi)
- [Persyaratan](#persyaratan)
- [Memulai](#memulai)
- [Mode demo](#mode-demo)
- [Konfigurasi](#konfigurasi)
- [Struktur proyek](#struktur-proyek)
- [Desain](#desain)
- [Pengujian](#pengujian)
- [Berkontribusi](#berkontribusi)
- [Kredit](#kredit)
- [Lisensi](#lisensi)

## Fitur

| Tab / layar | Yang bisa dilakukan |
|---|---|
| **Masuk & Daftar** | Satu akun per pengelola, terhubung ke RW yang dikelola (RW 4 atau RW 5). |
| **Sampah** | Ringkasan sampah 7 hari terakhir dengan grafik batang, riwayat catatan, dan form *Catat sampah* (tanggal, pukul, berat, catatan). |
| **Maggot** | Daftar siklus dengan "Hari ke-N" dan pita tahap (Telur → Tetas → Besar → Panen), total perkiraan panen, form *Mulai siklus* dan *Catat fase besar*. |
| **Detail siklus & Perkiraan panen** | Linimasa fase per siklus dan perkiraan hasil panen (kg) beserta data yang dipakai untuk menghitungnya. Perkiraan dihitung di server. |
| **GardaBot** | Tanya-jawab seputar budidaya maggot dan pemilahan sampah, dengan saran pertanyaan. |
| **Notifikasi** | Notifikasi dikelompokkan per hari, dengan penanda belum dibaca. |

Tampilan dibuat untuk pengguna lapangan: teks besar, tombol lebar, bahasa sehari-hari, dan huruf kalimat (bukan kapital semua).

## Teknologi

- **Bahasa:** Kotlin
- **UI:** XML + View Binding, Material Components 3, Navigation Component (navigasi bawah)
- **Arsitektur:** MVVM (ViewModel + LiveData) dengan Repository
- **Dependency injection:** Hilt
- **Jaringan:** Retrofit, OkHttp, Gson
- **Penyimpanan lokal:** DataStore (token login)
- **Android:** minSdk 24 (Android 7.0), targetSdk 36

## Persyaratan

- Android Studio Ladybug atau lebih baru
- **JDK 17 atau 21** untuk Gradle. JDK 25 belum didukung Gradle 8.13 yang dipakai proyek ini; build akan gagal dengan pesan versi Java.
- Emulator atau HP dengan Android 7.0 ke atas

Kalau Android Studio memakai JDK 25 bawaan, ganti di **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**.

## Memulai

```bash
git clone https://github.com/favjiw/garda-hijau-mengger-mobile.git
```

1. Buka folder hasil clone di Android Studio, lalu tunggu Gradle sync selesai.
2. Pilih konfigurasi `app` dan jalankan (**Run**) ke emulator atau HP.
3. Di layar Masuk, buat akun lewat **Daftar** atau masuk dengan akun yang sudah ada.

Membangun APK dari terminal:

| Perintah | Hasil | Server |
|---|---|---|
| `./gradlew assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` | Server asli |
| `./gradlew assembleDemo` | `app/build/outputs/apk/demo/app-demo.apk` | Data contoh (mode demo) |

## Mode demo

Build **demo** (`./gradlew assembleDemo`) menjawab semua request API dengan data contoh, jadi aplikasi bisa dicoba tanpa server dan tanpa akun.

- Masuk dengan nama pengguna dan kata sandi apa saja.
- Data contoh: catatan sampah 10 hari terakhir, 3 siklus maggot, perkiraan panen, notifikasi, dan jawaban GardaBot.
- Data yang ditambahkan lewat aplikasi tersimpan di memori sampai aplikasi ditutup.
- Perkiraan panen di mode demo memakai rumus sederhana, bukan model AI server.
- Kodenya ada di `model/remote/mock/` (`MockApiInterceptor`, `MockApiStore`).

Build **debug** dan **release** selalu memakai server asli.

## Konfigurasi

| Pengaturan | Lokasi | Keterangan |
|---|---|---|
| `BASE_URL` | `app/build.gradle.kts` (`defaultConfig`) | `https://gardahijaukemdiktisaintek.digital/` untuk semua fitur, termasuk GardaBot. |
| `USE_MOCK_API` | `app/build.gradle.kts` | `true` hanya di build type `demo`. |

Tidak ada kunci API atau rahasia yang perlu diisi.

**Cara aplikasi memakai API**

- **Login:** `POST /api/auth/login` mengembalikan token (berlaku 7 hari) yang disimpan di DataStore. `AuthInterceptor` memasang header `Authorization: Bearer <token>` di setiap request.
- **Token kedaluwarsa:** saat aplikasi dibuka, `GET /api/auth/me` dipanggil. Balasan 401 dari endpoint mana pun menghapus token dan membuka halaman Masuk.
- **Pesan galat:** server membalas `{ "success": false, "message": "..." }`. `PesanGalat` menampilkan `message` apa adanya, dengan pesan bawaan untuk 401, 404, 429 (terlalu banyak permintaan), 503 (layanan AI tidak tersedia), dan masalah jaringan.
- **Perkiraan panen:** form *Catat fase besar* mengirim fase `PEMBESARAN`, lalu `PANEN` dengan `jumlahTelur` dan `jumlahMakanan` (keduanya **gram**). Fase `PANEN` memicu prediksi AI (bisa ±15 detik). Kalau gagal, perkiraan bisa dihitung ulang dari Detail siklus.
- **Media telur:** pilihan diambil dari `GET /api/prediksi/opsi`, tidak di-hardcode.
- **GardaBot:** `POST /api/chatbot/tanya` dengan `{ "pertanyaan": "..." }`; hanya `data.jawaban` yang ditampilkan.

## Struktur proyek

```
app/src/main/java/com/codelabs/gardahijau/
├── di/                 # Modul Hilt
├── model/
│   ├── local/          # DataStore (UserPreferences) dan model lokal
│   └── remote/
│       ├── body/       # Body request API
│       ├── response/   # Class response API
│       ├── network/    # Retrofit service & ApiConfig
│       └── mock/       # Data contoh untuk mode demo
├── repository/         # Akses data untuk ViewModel
├── ui/
│   ├── auth/           # Masuk & Daftar
│   ├── pencatatan_history/  # Tab Sampah & Catat sampah
│   ├── prediksi/       # Tab Maggot, detail siklus, fase, perkiraan panen
│   ├── chatbot/        # Tab GardaBot
│   ├── notifications/  # Notifikasi
│   ├── adapter/        # Adapter RecyclerView
│   ├── widget/         # View kustom (grafik mingguan, pita siklus)
│   └── util/           # Inset sistem, format tanggal & angka
└── utils/              # Enum (RW, media telur)
```

## Desain

Tampilan mengikuti file Figma **"Garda Hijau Mengger — Redesign v2"**.

- **Warna:** `res/values/colors.xml`, awalan `ghm_` (sama dengan variabel Figma *GHM v2 / Warna*).
- **Teks & komponen:** `res/values/styles_ghm.xml`. `Text.GHM.*` dan `Widget.GHM.*` dinamai mengikuti text style dan komponen Figma.
- **Teks antarmuka:** `res/values/strings_ghm.xml`, awalan `ghm_`.
- **Huruf:** Bricolage Grotesque (judul, angka) dan Atkinson Hyperlegible Next (teks), di `res/font`.

Saat menambah layar, pakai warna `@color/ghm_*` dan style yang sudah ada. Jangan menulis kode warna hex atau ukuran huruf langsung di layout.

## Pengujian

`app/src/test/.../ApiKontrakTest.kt` memastikan model aplikasi bisa membaca contoh JSON dari dokumentasi API dan pesan galat sesuai status code. Jalankan:

```bash
./gradlew testDebugUnitTest
```

Untuk melihat balasan asli server dengan akun sendiri, jalankan `scripts/cek-api.sh`. Script ini meminta nama pengguna dan kata sandi, lalu menyimpan balasan tiap endpoint di `build/cek-api/` tanpa token.

Alur di layar (masuk, siklus, perkiraan panen, GardaBot) dicek manual di HP atau emulator.

## Berkontribusi

1. Buat branch dari `main`, misalnya `fitur/nama-fitur`.
2. Ikuti pola desain di atas dan pertahankan ID view yang dipakai Kotlin.
3. Pastikan `./gradlew assembleDebug` berhasil dan cek perubahan di emulator.
4. Tulis pesan commit dalam bahasa Indonesia yang menjelaskan apa yang berubah.
5. Buka Pull Request ke `main`.

## Kredit

- Dikembangkan dari aplikasi **WeGot** karya tim UNIKOM CodeLabs ([Haikalimanf/WeGot](https://github.com/Haikalimanf/WeGot), branch `dev`, commit `02ff078`), diunggah ulang dengan izin pembuatnya. Perubahan awal: nama aplikasi, package (`com.codelabs.wegot` → `com.codelabs.gardahijau`), tema, nama proyek, dan nama chatbot. Setelah itu seluruh tampilan didesain ulang (Redesign v2).
- Didukung program BEM Mahasiswa Berdampak 2026, Diktisaintek Berdampak, Wise on Waste, dan Universitas Komputer Indonesia.

## Lisensi

- Huruf Bricolage Grotesque dan Atkinson Hyperlegible Next memakai lisensi SIL Open Font License 1.1. Teks lisensinya ada di folder [`licenses/`](licenses/).
- Lisensi untuk kode aplikasi belum ditentukan. Hubungi pemilik repositori sebelum memakai ulang kode ini.
