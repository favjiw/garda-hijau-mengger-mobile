package com.codelabs.gardahijau.ui.prediksi.tambah_fase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFaseResponse
import com.codelabs.gardahijau.repository.PrediksiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TambahFaseViewModel @Inject constructor(
    private val repository: PrediksiRepository
) : ViewModel() {

    private val _addFaseResult = MutableLiveData<ApiResponse<AddFaseResponse>>()
    val addFaseResult: LiveData<ApiResponse<AddFaseResponse>> = _addFaseResult

    fun addFase(
        siklusId: Int,
        jenis: String,
        tanggal: String,
        jumlahTelur: Int,
        mediaTelur: String,
        catatan: String
    ) {
        viewModelScope.launch {
            _addFaseResult.value = ApiResponse.Empty
            val result = repository.addFase(siklusId, jenis, tanggal, jumlahTelur, mediaTelur, catatan)
            _addFaseResult.value = result
        }
    }

    fun addFasePembesaranWithPanen(
        siklusId: Int,
        tanggalPembesaran: String,
        jumlahMakanan: Int,
        catatan: String
    ) {
        viewModelScope.launch {
            _addFaseResult.value = ApiResponse.Empty

            try {
                val pembesaranResult = repository.addFasePembesaran(
                    siklusId = siklusId,
                    jenis = "PEMBESARAN",
                    tanggal = tanggalPembesaran,
                    jumlahMakanan = jumlahMakanan,
                    catatan = catatan
                )

                if (pembesaranResult is ApiResponse.Success) {
                    try {
                        repository.addFasePanen(
                            siklusId = siklusId,
                            jenis = "PANEN",
                            tanggal = tanggalPembesaran,
                            jumlahHasil = 0,
                            catatan = "Fase panen otomatis"
                        )
                    } catch (e: Exception) {
                        Log.w("TambahFaseViewModel", "Fase panen error: ${e.message}")
                    }

                    // Selalu return success karena PEMBESARAN berhasil
                    _addFaseResult.value = ApiResponse.Success(pembesaranResult.data)
                } else {
                    _addFaseResult.value = pembesaranResult
                }

            } catch (e: Exception) {
                _addFaseResult.value = ApiResponse.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

}
