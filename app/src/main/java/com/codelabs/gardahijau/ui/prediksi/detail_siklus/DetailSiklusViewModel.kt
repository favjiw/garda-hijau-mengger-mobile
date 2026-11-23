package com.codelabs.gardahijau.ui.prediksi.detail_siklus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.local.antrean.AntreanPerkiraan
import com.codelabs.gardahijau.model.local.antrean.JenisAntrean
import com.codelabs.gardahijau.model.local.antrean.KlasifikasiAntrean
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetFaseResponse
import com.codelabs.gardahijau.repository.AntreanRepository
import com.codelabs.gardahijau.repository.PrediksiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailSiklusViewModel @Inject constructor(
    private val repository: PrediksiRepository,
    private val antreanRepository: AntreanRepository,
) : ViewModel() {

    private val _faseData = MutableLiveData<ApiResponse<GetFaseResponse>>()
    val faseData: LiveData<ApiResponse<GetFaseResponse>> = _faseData

    private val _statusPerkiraan = MutableLiveData<ApiResponse<Unit>>()
    /** Empty = sedang menghitung (prediksi AI bisa ±15 detik), Success = selesai, Error = gagal. */
    val statusPerkiraan: LiveData<ApiResponse<Unit>> = _statusPerkiraan

    /** true = perhitungan perkiraan disimpan di HP dan akan dikirim otomatis. */
    private val _diantrekan = MutableLiveData<Boolean>()
    val diantrekan: LiveData<Boolean> = _diantrekan

    /** Kirim fase PANEN untuk memicu prediksi AI (dipakai bila perkiraan sebelumnya gagal). */
    fun hitungPerkiraan(siklusId: Int, tanggal: String, telurGram: Int, pakanGram: Double) {
        if (_statusPerkiraan.value is ApiResponse.Empty) return
        viewModelScope.launch {
            _statusPerkiraan.value = ApiResponse.Empty
            when (val hasil = repository.addFasePanen(siklusId, tanggal, telurGram, pakanGram, "")) {
                is ApiResponse.Success -> _statusPerkiraan.value = ApiResponse.Success(Unit)
                is ApiResponse.Error -> {
                    val penyebab = hasil.penyebab
                    if (penyebab != null && KlasifikasiAntrean.aksi(penyebab) == KlasifikasiAntrean.Aksi.ULANGI) {
                        antreanRepository.antrekan(
                            JenisAntrean.PERKIRAAN,
                            AntreanPerkiraan(siklusId, tanggal, telurGram, pakanGram),
                        )
                        _diantrekan.value = true
                    } else {
                        _statusPerkiraan.value = hasil
                    }
                }
                ApiResponse.Empty -> _statusPerkiraan.value = ApiResponse.Error("")
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
