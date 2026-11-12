package com.codelabs.gardahijau.model.remote.network

import android.content.Context
import com.codelabs.gardahijau.R
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException

/**
 * Mengubah error dari Retrofit/OkHttp menjadi pesan yang bisa langsung ditampilkan ke pengguna.
 *
 * Server selalu membalas error dengan `{ "success": false, "message": "..." }`, dan `message`
 * boleh ditampilkan apa adanya. Kalau tidak ada `message`, dipakai pesan bawaan per status code.
 * Dipasang sekali di [com.codelabs.gardahijau.MyApp.onCreate].
 */
object PesanGalat {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun dari(error: Throwable): String = when (error) {
        is HttpException -> dariHttp(error.code(), bacaMessage(error))
        // Timeout juga turunan IOException, jadi dicek lebih dulu.
        is InterruptedIOException -> teks(R.string.ghm_galat_waktu_habis)
        is IOException -> teks(R.string.ghm_galat_jaringan)
        else -> teks(R.string.ghm_galat_umum)
    }

    /** Pesan untuk status code tertentu; [message] dari server dipakai bila ada (kecuali 401). */
    fun dariHttp(code: Int, message: String?): String = when {
        // 401 selalu diarahkan ke halaman Masuk (lihat AuthInterceptor); pesan server-nya teknis.
        code == 401 -> teks(R.string.ghm_galat_sesi)
        !message.isNullOrBlank() -> message
        code == 404 -> teks(R.string.ghm_galat_404)
        code == 429 -> teks(R.string.ghm_galat_429)
        code == 503 -> teks(R.string.ghm_galat_503)
        else -> teks(R.string.ghm_galat_umum)
    }

    /** Ambil field `message` dari body error JSON. Body hanya bisa dibaca sekali. */
    fun bacaMessage(error: HttpException): String? = try {
        val body = error.response()?.errorBody()?.string()
        if (body.isNullOrBlank()) {
            null
        } else {
            val json = JsonParser.parseString(body)
            if (json.isJsonObject) json.asJsonObject.get("message")?.takeIf { it.isJsonPrimitive }?.asString else null
        }
    } catch (e: Exception) {
        null
    }

    private fun teks(id: Int): String =
        appContext?.getString(id) ?: CADANGAN[id] ?: "Terjadi kesalahan. Coba lagi."

    /** Dipakai di unit test (tanpa Context). Isinya sama dengan strings_ghm.xml. */
    private val CADANGAN = mapOf(
        R.string.ghm_galat_jaringan to "Tidak bisa terhubung ke server. Periksa internet lalu coba lagi.",
        R.string.ghm_galat_waktu_habis to "Server terlalu lama menjawab. Coba lagi.",
        R.string.ghm_galat_sesi to "Sesi kamu sudah berakhir. Silakan masuk lagi.",
        R.string.ghm_galat_404 to "Data tidak ditemukan.",
        R.string.ghm_galat_429 to "Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi.",
        R.string.ghm_galat_503 to "Layanan AI sedang tidak tersedia. Coba lagi nanti.",
        R.string.ghm_galat_umum to "Terjadi kesalahan. Coba lagi.",
    )
}

/** Singkatan: `ApiResponse.Error(e.pesanGalat())`. */
fun Throwable.pesanGalat(): String = PesanGalat.dari(this)
