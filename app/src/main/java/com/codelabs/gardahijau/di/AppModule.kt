package com.codelabs.gardahijau.di

import android.content.Context
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.network.ApiConfig
import com.codelabs.gardahijau.model.remote.network.MainApiService
import com.codelabs.gardahijau.model.remote.network.ChatbotApiService
import com.codelabs.gardahijau.model.remote.network.FlaskApiService
import com.codelabs.gardahijau.model.remote.response.RemoteDataSource
import com.codelabs.gardahijau.repository.PrediksiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideMainApiService(userPreferences: UserPreferences): MainApiService {
        return ApiConfig.getMainApiService(userPreferences)
    }

    @Provides
    fun provideChatbotApiService(): ChatbotApiService {
        return ApiConfig.getChatbotApiService()
    }

    @Provides
    fun provideFlaskApiService(): FlaskApiService {
        return ApiConfig.getFlaskApiService()
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences =
        UserPreferences(context)

    @Provides
    fun providePrediksiRepository(
        remoteDataSource: RemoteDataSource,
    ): PrediksiRepository {
        return PrediksiRepository(remoteDataSource)
    }
}