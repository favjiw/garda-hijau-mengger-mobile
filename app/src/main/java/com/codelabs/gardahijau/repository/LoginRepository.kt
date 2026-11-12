package com.codelabs.gardahijau.repository

import com.codelabs.gardahijau.model.remote.network.pesanGalat
import com.codelabs.gardahijau.model.remote.network.ApiConfig
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.RemoteDataSource
import com.codelabs.gardahijau.model.remote.response.auth.login.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {

    fun login(username: String, password: String): Flow<ApiResponse<LoginResponse>> = flow {
        try {
            emit(ApiResponse.Empty)
            val response = remoteDataSource.loginAccount(username, password)
            emit(ApiResponse.Success(response))
        } catch (e: Exception) {
            emit(ApiResponse.Error(e.pesanGalat()))
        }
    }.flowOn(Dispatchers.IO)

}