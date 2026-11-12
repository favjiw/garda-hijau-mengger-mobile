package com.codelabs.gardahijau.model.remote.network

import com.codelabs.gardahijau.BuildConfig
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.mock.MockApiInterceptor
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

            val clientBuilder = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
            if (BuildConfig.USE_MOCK_API) {
                clientBuilder.addInterceptor(MockApiInterceptor())
            }
            val client = clientBuilder.build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.smartmaggot.my.id/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(MainApiService::class.java)
        }


        fun getChatbotApiService(): ChatbotApiService {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

            val builder = Retrofit.Builder()
                .baseUrl("https://chatbot.smartmaggot.my.id/")
                .addConverterFactory(GsonConverterFactory.create())
            if (BuildConfig.USE_MOCK_API) {
                builder.client(OkHttpClient.Builder().addInterceptor(MockApiInterceptor()).build())
            }
            val retrofit = builder.build()

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