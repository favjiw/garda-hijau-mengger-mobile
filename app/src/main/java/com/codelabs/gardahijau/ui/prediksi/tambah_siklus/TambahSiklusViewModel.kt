package com.codelabs.gardahijau.ui.prediksi.tambah_siklus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.local.antrean.AntreanSiklus
import com.codelabs.gardahijau.model.local.antrean.JenisAntrean
import com.codelabs.gardahijau.model.local.antrean.KlasifikasiAntrean
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddSiklusResponse
import com.codelabs.gardahijau.repository.AntreanRepository
import com.codelabs.gardahijau.repository.PrediksiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TambahSiklusViewModel @Inject constructor(
    private val repository: PrediksiRepository,
    private val antreanRepository: AntreanRepository,
) : ViewModel() {

    private val _addSiklusResult = MutableLiveData<ApiResponse<AddSiklusResponse>>()
    val addSiklusResult: LiveData<ApiResponse<AddSiklusResponse>> = _addSiklusResult

    /** true = siklus disimpan di HP dan akan dikirim otomatis (tidak ada internet). */
    private val _diantrekan = MutableLiveData<Boolean>()
    val diantrekan: LiveData<Boolean> = _diantrekan

    private val _opsiMedia = MutableLiveData<ApiResponse<List<String>>>()
    /** Pilihan media telur dari server (GET /api/prediksi/opsi). Empty = sedang memuat. */
    val opsiMedia: LiveData<ApiResponse<List<String>>> = _opsiMedia

    fun muatOpsiMedia() {
        if (_opsiMedia.value is ApiResponse.Empty) return
        viewModelScope.launch {
            _opsiMedia.value = ApiResponse.Empty
            _opsiMedia.value = when (val hasil = repository.getOpsi()) {
                is ApiResponse.Success -> ApiResponse.Success(hasil.data.mediaTelur.orEmpty())
                is ApiResponse.Error -> hasil
                ApiResponse.Empty -> ApiResponse.Error("")
            }
        }
    }

    fun addSiklus(tanggalMulai: String, jumlahTelur: Int, mediaTelur: String, catatan: String) {
        viewModelScope.launch {
            _addSiklusResult.value = ApiResponse.Empty
            val result = repository.addSiklus(tanggalMulai, jumlahTelur, mediaTelur, catatan)
            val penyebab = (result as? ApiResponse.Error)?.penyebab
            if (penyebab != null && KlasifikasiAntrean.aksi(penyebab) == KlasifikasiAntrean.Aksi.ULANGI) {
                // Tidak ada internet: simpan di HP, kirim otomatis saat online.
                antreanRepository.antrekan(
                    JenisAntrean.SIKLUS,
                    AntreanSiklus(tanggalMulai, jumlahTelur, mediaTelur, catatan),
                )
                _diantrekan.value = true
            } else {
                _addSiklusResult.value = result
            }
        }
    }
}
