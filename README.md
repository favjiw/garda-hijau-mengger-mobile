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
