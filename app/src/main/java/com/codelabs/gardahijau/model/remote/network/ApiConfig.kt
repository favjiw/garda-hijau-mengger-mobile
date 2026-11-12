package com.codelabs.gardahijau.model.remote.network

import android.content.Context
import com.codelabs.gardahijau.BuildConfig
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.mock.MockApiInterceptor
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Satu Retrofit untuk semua endpoint (akun, sampah, siklus, prediksi, notifikasi, chatbot).
 * Base URL dari BuildConfig.BASE_URL (app/build.gradle.kts).
 */
class ApiConfig {
    companion object {
        fun getMainApiService(context: Context, userPreferences: UserPreferences): MainApiService {
            val clientBuilder = OkHttpClient.Builder()
                // Server menunggu layanan AI sampai 15 detik (prediksi) dan 60 detik (chatbot),
                // lihat ML_TIMEOUT_MS & CHATBOT_TIMEOUT_MS di BE-BEM. Aplikasi harus menunggu lebih lama.
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(75, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .callTimeout(90, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(context.applicationContext, userPreferences))

            if (BuildConfig.DEBUG) {
                // Log hanya di build debug; header token & body login tidak ikut tercatat di release.
                val logging = HttpLoggingInterceptor { pesan ->
                    // Token di balasan login dan kata sandi di body login tidak boleh tercatat.
                    Platform.get().log(sensorLog(pesan))
                }.apply {
                    level = HttpLoggingInterceptor.Level.BODY
                    redactHeader("Authorization")
                }
                clientBuilder.addInterceptor(logging)
            }
            if (BuildConfig.USE_MOCK_API) {
                clientBuilder.addInterceptor(MockApiInterceptor())
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build()

            return retrofit.create(MainApiService::class.java)
        }

        private val POLA_RAHASIA = Regex("""("token"|"password")\s*:\s*"[^"]*"""")

        /** Ganti nilai "token" dan "password" di log dengan ***. */
        fun sensorLog(pesan: String): String =
            POLA_RAHASIA.replace(pesan) { "${it.groupValues[1]}:\"***\"" }
    }
}
