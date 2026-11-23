package com.codelabs.gardahijau.model.local

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Penyimpanan foto lokal per catatan sampah.
 * Foto TIDAK dikirim ke server (API belum menerima gambar); kuncinya = tanggalWaktu catatan,
 * jadi foto tetap ketemu baik catatan tersimpan langsung maupun lewat antrean offline.
 */
object FotoLokal {

    private fun folder(context: Context) = File(context.filesDir, "foto-sampah").apply { mkdirs() }

    private fun namaFile(kunci: String) = kunci.replace(Regex("[^A-Za-z0-9]"), "-") + ".jpg"

    /** Salin foto pilihan pengguna ke penyimpanan aplikasi. Mengembalikan file, atau null bila gagal. */
    fun simpan(context: Context, kunci: String, sumber: Uri): File? = try {
        val tujuan = File(folder(context), namaFile(kunci))
        context.contentResolver.openInputStream(sumber)?.use { input ->
            tujuan.outputStream().use { output -> input.copyTo(output) }
        }
        tujuan.takeIf { it.exists() && it.length() > 0 }
    } catch (e: Exception) {
        null
    }

    /** Foto untuk catatan dengan [kunci], atau null bila tidak ada. */
    fun ambil(context: Context, kunci: String?): File? {
        if (kunci.isNullOrBlank()) return null
        return File(folder(context), namaFile(kunci)).takeIf { it.exists() && it.length() > 0 }
    }
}
