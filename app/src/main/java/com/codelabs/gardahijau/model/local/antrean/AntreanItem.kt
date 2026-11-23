package com.codelabs.gardahijau.model.local.antrean

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Jenis permintaan yang bisa disimpan di antrean offline. */
object JenisAntrean {
    const val SAMPAH = "SAMPAH"
    const val SIKLUS = "SIKLUS"
    const val FASE_BESAR = "FASE_BESAR"
    const val PERKIRAAN = "PERKIRAAN"
}

/**
 * Satu permintaan input yang belum terkirim ke server (mis. saat tidak ada internet).
 * [payload] berisi JSON dari salah satu kelas di bawah, sesuai [jenis].
 */
@Entity(tableName = "antrean_kirim")
data class AntreanItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jenis: String,
    val payload: String,
    val dibuatPada: Long,
    val percobaan: Int = 0,
)

/** Payload POST /api/pencacahan. */
data class AntreanSampah(
    val tanggalWaktu: String,
    val totalSampah: Double,
    val catatan: String,
)

/** Payload POST /api/siklus (+ fase PENETASAN yang menyusul di worker). */
data class AntreanSiklus(
    val tanggalMulai: String,
    val jumlahTelur: Int,
    val mediaTelur: String,
    val catatan: String,
)

/** Payload fase PEMBESARAN + PANEN (pemicu prediksi) untuk satu siklus. */
data class AntreanFaseBesar(
    val siklusId: Int,
    val tanggal: String,
    val pakanKg: Double,
    val telurGram: Int,
    val keterangan: String,
)

/** Payload fase PANEN saja (menghitung ulang perkiraan yang sempat gagal). */
data class AntreanPerkiraan(
    val siklusId: Int,
    val tanggal: String,
    val telurGram: Int,
    val pakanGram: Double,
)
