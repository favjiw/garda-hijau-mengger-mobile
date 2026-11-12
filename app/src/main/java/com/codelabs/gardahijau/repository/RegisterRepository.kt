package com.codelabs.gardahijau.repository

import com.codelabs.gardahijau.model.remote.network.ApiConfig
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.RemoteDataSource
import com.codelabs.gardahijau.model.remote.response.auth.register.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class RegisterRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {

    fun register(username: String, password: String, rw: String): Flow<ApiResponse<RegisterResponse>> = flow {
        try {
            emit(ApiResponse.Empty)
            val response = remoteDataSource.registerAccount(username, password, rw)
            emit(ApiResponse.Success(response))
        } catch (e: Exception) {
            emit(ApiResponse.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)
}