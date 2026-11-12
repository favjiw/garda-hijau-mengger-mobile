# Garda Hijau Mengger — Aplikasi Android

Aplikasi Android untuk pengelola dan peternak maggot BSF di Kelurahan Mengger. Program **BEM Mahasiswa Berdampak 2026 — Sinergi Garda Hijau Mengger**. Didukung sistem Smart Maggot (`smartmaggot.my.id`).

## Fitur

- **Login & register** akun pengelola per RW
- **Pencacahan sampah**: catat tanggal, waktu, total sampah (kg), dan catatan; riwayat + grafik
- **Siklus maggot**: mulai siklus (tanggal, jumlah telur, media telur), lalu tambah fase Penetasan → Pembesaran → Panen
- **Prediksi panen**: estimasi hasil panen dari jumlah telur dan pakan (model ML via layanan Flask)
- **GardaBot**: chatbot tanya-jawab seputar maggot dan pemilahan sampah
- **Notifikasi** aktivitas

## Teknologi

Kotlin, View Binding, MVVM (ViewModel + LiveData), Hilt, Retrofit + OkHttp, Room, DataStore, Navigation Component, Material Components. minSdk 24, targetSdk 36.

## Layanan backend

| Layanan | Base URL |
|---|---|
| API utama (BE-BEM) | `https://api.smartmaggot.my.id/` |
| Prediksi panen (Flask) | `https://flaskapi.smartmaggot.my.id/` |
| Chatbot | `https://chatbot.smartmaggot.my.id/` |

URL diatur di `app/src/main/java/com/codelabs/gardahijau/model/remote/network/ApiConfig.kt`.

## Menjalankan

1. Buka folder ini di Android Studio (Ladybug atau lebih baru).
2. Tunggu Gradle sync, lalu Run ke emulator atau HP (Android 7.0+).

## Kredit

Hasil rebranding dari aplikasi **WeGot** karya tim UNIKOM CodeLabs ([Haikalimanf/WeGot](https://github.com/Haikalimanf/WeGot), branch `dev`, commit `02ff078`), diunggah ulang dengan izin pembuatnya. Perubahan: nama aplikasi, package (`com.codelabs.wegot` → `com.codelabs.gardahijau`), tema, nama proyek, dan nama chatbot.

## Desain (Redesign v2)

Tampilan mengikuti file Figma **"Garda Hijau Mengger — Redesign v2"** (section di halaman *Mobile Design*).

- Warna: `res/values/colors.xml` (awalan `ghm_`, sama dengan variabel Figma `GHM v2 / Warna`)
- Teks & komponen: `res/values/styles_ghm.xml` (`Text.GHM.*`, `Widget.GHM.*`, nama mengikuti text style Figma)
- Huruf: Bricolage Grotesque (judul, angka) dan Atkinson Hyperlegible Next (teks), di `res/font`. Lisensi SIL OFL 1.1, lihat folder `licenses/`.
- Layar yang sudah memakai desain baru: Masuk, Daftar, Beranda Sampah, navigasi bawah.

## Mode demo (tanpa server)

Build **debug** memakai data contoh (`USE_MOCK_API = true` di `app/build.gradle.kts`), jadi aplikasi bisa dicoba walau server smartmaggot.my.id mati.

- Masuk dengan nama pengguna dan kata sandi apa saja.
- Data contoh: catatan sampah 10 hari terakhir, 3 siklus maggot, perkiraan panen, notifikasi, jawaban GardaBot.
- Data yang ditambahkan lewat aplikasi tersimpan di memori sampai aplikasi ditutup.
- Kode: `model/remote/mock/` (`MockApiInterceptor`, `MockApiStore`). Perkiraan panen di mode demo memakai rumus sederhana, bukan model ML.
- Untuk memakai server asli di build debug, ubah `USE_MOCK_API` di blok `debug` menjadi `"false"` lalu sync. Build **release** selalu memakai server asli.
