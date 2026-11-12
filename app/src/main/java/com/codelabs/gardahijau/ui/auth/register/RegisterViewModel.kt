package com.codelabs.gardahijau.ui.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.auth.register.RegisterResponse
import com.codelabs.gardahijau.repository.RegisterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerRepository: RegisterRepository
) : ViewModel() {

    private val _registerResult = MutableLiveData<ApiResponse<RegisterResponse>>()
    val registerResult: LiveData<ApiResponse<RegisterResponse>> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(username: String, password: String, rw: String) {
        viewModelScope.launch {
            registerRepository.register(username, password, rw).collect { result ->
                when (result) {
                    is ApiResponse.Empty -> _isLoading.value = true
                    is ApiResponse.Success -> {
                        _isLoading.value = false
                        _registerResult.value = result
                    }
                    is ApiResponse.Error -> {
                        _isLoading.value = false
                        _registerResult.value = result
                    }
                }
            }
        }
    }
}