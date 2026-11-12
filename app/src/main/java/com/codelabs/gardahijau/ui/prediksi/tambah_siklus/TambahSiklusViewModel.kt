package com.codelabs.gardahijau.ui.prediksi.tambah_siklus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddSiklusResponse
import com.codelabs.gardahijau.repository.PrediksiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TambahSiklusViewModel @Inject constructor(
    private val repository: PrediksiRepository
) : ViewModel() {

    private val _addSiklusResult = MutableLiveData<ApiResponse<AddSiklusResponse>>()
    val addSiklusResult: LiveData<ApiResponse<AddSiklusResponse>> = _addSiklusResult

    fun addSiklus(tanggalMulai: String, jumlahTelur: Int, mediaTelur: String, catatan: String) {
        viewModelScope.launch {
            _addSiklusResult.value = ApiResponse.Empty
            val result = repository.addSiklus(tanggalMulai, jumlahTelur, mediaTelur, catatan)
            _addSiklusResult.value = result
        }
    }
}
