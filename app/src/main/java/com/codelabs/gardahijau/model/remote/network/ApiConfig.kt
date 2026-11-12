package com.codelabs.gardahijau.model.remote.network

import com.codelabs.gardahijau.model.local.data.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig {
    companion object {
        fun getMainApiService(userPreferences: UserPreferences): MainApiService {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val token = runBlocking { userPreferences.getAuthToken().firstOrNull().orEmpty() }

                val requestBuilder = original.newBuilder()
                    .addHeader("Content-Type", "application/json")
                if (token.isNotEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                val request = requestBuilder.build()
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.smartmaggot.my.id/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(MainApiService::class.java)
        }


        fun getChatbotApiService(): ChatbotApiService {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://chatbot.smartmaggot.my.id/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ChatbotApiService::class.java)
        }

        fun getFlaskApiService(): FlaskApiService {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://flaskapi.smartmaggot.my.id/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(FlaskApiService::class.java)
        }


    }

}