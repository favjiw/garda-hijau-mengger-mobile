package com.codelabs.gardahijau.repository

import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.network.MainApiService
import javax.inject.Inject

class LogoutRepository @Inject constructor(
    private val api: MainApiService,
    private val userPreferences: UserPreferences
) {

    suspend fun logout(): ApiResponse<Unit> {
        return try {
            api.logout()
            userPreferences.clear()
            ApiResponse.Success(Unit)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }
}
