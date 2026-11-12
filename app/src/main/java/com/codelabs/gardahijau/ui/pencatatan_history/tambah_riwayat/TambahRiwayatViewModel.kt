package com.codelabs.gardahijau.ui.pencatatan_history.tambah_riwayat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.AddHistoryPencacahanResponse
import com.codelabs.gardahijau.repository.HistoryPencacahanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TambahRiwayatViewModel @Inject constructor(
    private val historyPencacahanRepository: HistoryPencacahanRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _addHistoryResult = MutableLiveData<AddHistoryPencacahanResponse>()
    val addHistoryResult: LiveData<AddHistoryPencacahanResponse> = _addHistoryResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun addHistoryPencacahan(tanggalWaktu: String, totalSampah: Double, catatan: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = historyPencacahanRepository.addHistoryPencacahan(
                    tanggalWaktu, totalSampah, catatan
                )
                _addHistoryResult.value = response
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

}
