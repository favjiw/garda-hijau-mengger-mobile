package com.codelabs.gardahijau.model.remote.network

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.ui.auth.login.LoginActivity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean

/**
 * - Menambahkan header `Authorization: Bearer <token>` bila token ada.
 * - Bila server membalas 401 (token kedaluwarsa / tidak valid), token dihapus lalu pengguna
 *   diarahkan ke halaman Masuk. Tidak berlaku untuk login, register, dan logout.
 *
 * Token dihapus lebih dulu (sinkron) supaya LoginActivity tidak langsung melempar balik ke
 * MainActivity karena masih menemukan token.
 */
class AuthInterceptor(
    private val appContext: Context,
    private val userPreferences: UserPreferences,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = runBlocking { userPreferences.getAuthToken().firstOrNull().orEmpty() }

        val request = if (token.isNotEmpty()) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            original
        }

        val response = chain.proceed(request)
        val path = original.url.encodedPath
        val tanpaArahkan = path.endsWith("/auth/login") || path.endsWith("/auth/register") || path.endsWith("/auth/logout")
        if (response.code == 401 && !tanpaArahkan) {
            sesiBerakhir()
        }
        return response
    }

    private fun sesiBerakhir() {
        // Beberapa request bisa gagal 401 bersamaan; cukup arahkan sekali.
        if (!sedangMengarahkan.compareAndSet(false, true)) return
        runBlocking { userPreferences.clear() }
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(appContext, R.string.ghm_galat_sesi, Toast.LENGTH_LONG).show()
            val intent = Intent(appContext, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            appContext.startActivity(intent)
        }
    }

    companion object {
        private val sedangMengarahkan = AtomicBoolean(false)

        /** Dipanggil setelah login berhasil supaya 401 berikutnya bisa mengarahkan lagi. */
        fun reset() = sedangMengarahkan.set(false)
    }
}
