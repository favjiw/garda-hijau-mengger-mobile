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

    fun getFase(siklusId: Int) {
        viewModelScope.launch {
            _faseData.value = ApiResponse.Empty
            val result = repository.getFase(siklusId)
            _faseData.value = result
        }
    }
}
