package com.codelabs.gardahijau.ui.prediksi.tambah_fase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.repository.PrediksiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Status simpan fase besar + perkiraan panen, untuk ditampilkan di TambahFaseActivity. */
sealed class StatusFaseBesar {
    /** Sedang menyimpan fase PEMBESARAN. */
    object MenyimpanFase : StatusFaseBesar()
    /** Fase besar tersimpan; sedang menunggu prediksi AI dari fase PANEN (bisa ±15 detik). */
    object MenghitungPerkiraan : StatusFaseBesar()
    /** Fase besar & perkiraan panen tersimpan. */
    data class Selesai(val hasilKg: Double?) : StatusFaseBesar()
    /** Fase besar tersimpan, tapi perkiraan gagal. Bisa dicoba lagi dari Detail siklus. */
    data class PerkiraanGagal(val pesan: String) : StatusFaseBesar()
    /** Fase besar gagal disimpan; tidak ada yang tersimpan. */
    data class Gagal(val pesan: String) : StatusFaseBesar()
}

@HiltViewModel
class TambahFaseViewModel @Inject constructor(
    private val repository: PrediksiRepository
) : ViewModel() {

    private val _status = MutableLiveData<StatusFaseBesar>()
    val status: LiveData<StatusFaseBesar> = _status

    /**
     * Simpan fase PEMBESARAN, lalu fase PANEN (jumlah telur & pakan dalam gram) yang memicu
     * prediksi AI di server.
     */
    fun simpanFaseBesar(
        siklusId: Int,
        tanggal: String,
        pakanKg: Double,
        telurGram: Int,
        keterangan: String,
        denganFaseBesar: Boolean = true,
    ) {
        if (_status.value is StatusFaseBesar.MenyimpanFase || _status.value is StatusFaseBesar.MenghitungPerkiraan) return
        viewModelScope.launch {
            val pakanGram = pakanKg * 1000.0

            if (denganFaseBesar) {
                _status.value = StatusFaseBesar.MenyimpanFase
                val pembesaran = repository.addFasePembesaran(siklusId, tanggal, pakanGram, keterangan)
                if (pembesaran is ApiResponse.Error) {
                    _status.value = StatusFaseBesar.Gagal(pembesaran.errorMessage)
                    return@launch
                }
            }

            _status.value = StatusFaseBesar.MenghitungPerkiraan
            _status.value = when (val panen = repository.addFasePanen(siklusId, tanggal, telurGram, pakanGram, keterangan)) {
                is ApiResponse.Success -> StatusFaseBesar.Selesai(panen.data.data?.prediksiPanen?.hasilKg)
                is ApiResponse.Error -> StatusFaseBesar.PerkiraanGagal(panen.errorMessage)
                ApiResponse.Empty -> StatusFaseBesar.PerkiraanGagal("")
            }
        }
    }
}
