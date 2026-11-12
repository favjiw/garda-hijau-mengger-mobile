package com.codelabs.gardahijau.ui.pencatatan_history

import com.codelabs.gardahijau.model.remote.network.pesanGalat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.GetHistoryPencacahanResponse
import com.codelabs.gardahijau.repository.HistoryPencacahanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PencatatanHistoryViewModel @Inject constructor(
    private val historyPencacahanRepository: HistoryPencacahanRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _historyData = MutableLiveData<GetHistoryPencacahanResponse>()
    val historyData: LiveData<GetHistoryPencacahanResponse> = _historyData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getHistoryPencacahan() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = historyPencacahanRepository.getHistoryPencacahan()
                _historyData.value = response
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.pesanGalat()
                _isLoading.value = false
            }
        }
    }
}
