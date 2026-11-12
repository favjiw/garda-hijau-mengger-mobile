package com.codelabs.gardahijau.ui.prediksi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenDashboardResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetSiklusResponse
import com.codelabs.gardahijau.repository.PrediksiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrediksiViewModel @Inject constructor(
    private val repository: PrediksiRepository
) : ViewModel() {
    private val _siklusData = MutableLiveData<ApiResponse<GetSiklusResponse>>()
    val siklusData: LiveData<ApiResponse<GetSiklusResponse>> = _siklusData

    fun getSiklus() {
        viewModelScope.launch {
            _siklusData.value = ApiResponse.Empty
            val result = repository.getSiklus()
            _siklusData.value = result
        }
    }

    suspend fun fetchHasilPanenSiklus(siklusId: Int): ApiResponse<GetHasilPanenSiklusResponse> {
        return repository.getHasilPanenSiklus(siklusId)
    }

    /** Daftar fase satu siklus, untuk menentukan tahap di pita siklus. */
    suspend fun fetchFase(siklusId: Int): ApiResponse<GetFaseResponse> {
        return repository.getFase(siklusId)
    }

    private val _hasilDashboard = MutableLiveData<ApiResponse<GetHasilPanenDashboardResponse>>()
    val hasilDashboard: LiveData<ApiResponse<GetHasilPanenDashboardResponse>> = _hasilDashboard

    fun getHasilPanenDashboard() {
        viewModelScope.launch {
            _hasilDashboard.value = ApiResponse.Empty
            val result = repository.getHasilPanenDashboard()
            _hasilDashboard.value = result
        }
    }

}
