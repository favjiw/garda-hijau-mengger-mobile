package com.codelabs.gardahijau.ui.prediksi.detail_siklus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetFaseResponse
import com.codelabs.gardahijau.repository.PrediksiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailSiklusViewModel @Inject constructor(
    private val repository: PrediksiRepository
) : ViewModel() {

    private val _faseData = MutableLiveData<ApiResponse<GetFaseResponse>>()
    val faseData: LiveData<ApiResponse<GetFaseResponse>> = _faseData

    private val _statusPerkiraan = MutableLiveData<ApiResponse<Unit>>()
    /** Empty = sedang menghitung (prediksi AI bisa ±15 detik), Success = selesai, Error = gagal. */
    val statusPerkiraan: LiveData<ApiResponse<Unit>> = _statusPerkiraan

    /** Kirim fase PANEN untuk memicu prediksi AI (dipakai bila perkiraan sebelumnya gagal). */
    fun hitungPerkiraan(siklusId: Int, tanggal: String, telurGram: Int, pakanGram: Double) {
        if (_statusPerkiraan.value is ApiResponse.Empty) return
        viewModelScope.launch {
            _statusPerkiraan.value = ApiResponse.Empty
            _statusPerkiraan.value = when (val hasil = repository.addFasePanen(siklusId, tanggal, telurGram, pakanGram, "")) {
                is ApiResponse.Success -> ApiResponse.Success(Unit)
                is ApiResponse.Error -> hasil
                ApiResponse.Empty -> ApiResponse.Error("")
            }
        }
    }

    fun getFase(siklusId: Int) {
        viewModelScope.launch {
            _faseData.value = ApiResponse.Empty
            val result = repository.getFase(siklusId)
            _faseData.value = result
        }
    }
}
