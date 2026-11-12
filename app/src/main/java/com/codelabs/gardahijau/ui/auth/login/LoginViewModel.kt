package com.codelabs.gardahijau.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.auth.LogoutResponse
import com.codelabs.gardahijau.model.remote.response.auth.login.LoginResponse
import com.codelabs.gardahijau.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.clear

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _loginResult = MutableLiveData<ApiResponse<LoginResponse>>()
    val loginResult: LiveData<ApiResponse<LoginResponse>> = _loginResult

    private val _logoutResult = MutableLiveData<ApiResponse<LogoutResponse>>()
    val logoutResult: LiveData<ApiResponse<LogoutResponse>> = _logoutResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginRepository.login(username, password).collect { result ->
                when (result) {
                    is ApiResponse.Empty -> _isLoading.value = true
                    is ApiResponse.Success -> {
                        _isLoading.value = false
                        _loginResult.value = result
                        val token = result.data.token
                        if (!token.isNullOrEmpty()) {
                            viewModelScope.launch {
                                userPreferences.saveAuthToken(token)
                            }
                        }
                    }
                    is ApiResponse.Error -> {
                        _isLoading.value = false
                        _loginResult.value = result
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clear()
        }
    }

}