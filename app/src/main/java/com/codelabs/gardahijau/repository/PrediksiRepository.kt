package com.codelabs.gardahijau.repository

import android.util.Log
import com.codelabs.gardahijau.model.remote.body.FaseRequest
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.network.pesanGalat
import com.codelabs.gardahijau.model.remote.response.RemoteDataSource
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFasePanenResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenDashboardResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.Opsi
import javax.inject.Inject

class PrediksiRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) {
    suspend fun getSiklus(): ApiResponse<GetSiklusResponse> = panggil("getSiklus") {
        remoteDataSource.getSiklus()
    }

    /**
     * Buat siklus, lalu pastikan ada fase PENETASAN (jumlah telur dalam gram).
     * PENETASAN hanya ditambahkan bila server belum membuatnya, supaya tidak dobel.
     */
    suspend fun addSiklus(
        tanggalMulai: String,
        jumlahTelur: Int,
        mediaTelur: String,
        catatan: String
    ): ApiResponse<AddSiklusResponse> {
        val siklus = try {
            remoteDataSource.addSiklus(tanggalMulai, jumlahTelur, mediaTelur, catatan)
        } catch (e: Exception) {
            Log.e(TAG, "addSiklus gagal", e)
            return ApiResponse.Error(e.pesanGalat())
        }
        val siklusId = siklus.data?.id ?: return ApiResponse.Success(siklus)

        try {
            val sudahAda = remoteDataSource.getFase(siklusId).data.orEmpty()
                .any { it.jenis.equals(JENIS_PENETASAN, ignoreCase = true) }
            if (!sudahAda) {
                remoteDataSource.addFase(
                    siklusId,
                    FaseRequest(jenis = JENIS_PENETASAN, tanggal = tanggalMulai, jumlahTelur = jumlahTelur)
                )
            }
        } catch (e: Exception) {
            // Siklus sudah tersimpan; fase penetasan yang gagal tidak membatalkannya.
            Log.w(TAG, "Siklus dibuat, tapi fase PENETASAN gagal: ${e.pesanGalat()}")
        }
        return ApiResponse.Success(siklus)
    }

    suspend fun getFase(siklusId: Int): ApiResponse<GetFaseResponse> = panggil("getFase") {
        remoteDataSource.getFase(siklusId)
    }

    /** Fase PEMBESARAN. [pakanGram] disimpan sebagai catatan jumlah pakan (gram). */
    suspend fun addFasePembesaran(
        siklusId: Int,
        tanggal: String,
        pakanGram: Double,
        keterangan: String,
    ): ApiResponse<AddFaseResponse> = panggil("addFasePembesaran") {
        remoteDataSource.addFase(
            siklusId,
            FaseRequest(
                jenis = JENIS_PEMBESARAN,
                tanggal = tanggal,
                jumlahMakanan = pakanGram,
                keterangan = keterangan.ifBlank { null },
            )
        )
    }

    /**
     * Fase PANEN: memicu prediksi AI di server (bisa ±15 detik).
     * [telurGram] dan [pakanGram] wajib >= 1. Bila layanan AI tidak tersedia (503), fase tidak disimpan.
     */
    suspend fun addFasePanen(
        siklusId: Int,
        tanggal: String,
        telurGram: Int,
        pakanGram: Double,
        keterangan: String,
    ): ApiResponse<AddFasePanenResponse> = panggil("addFasePanen") {
        remoteDataSource.addFasePanen(
            siklusId,
            FaseRequest(
                jenis = JENIS_PANEN,
                tanggal = tanggal,
                jumlahTelur = telurGram,
                jumlahMakanan = pakanGram,
                keterangan = keterangan.ifBlank { null },
            )
        )
    }

    suspend fun getHasilPanenSiklus(siklusId: Int): ApiResponse<GetHasilPanenSiklusResponse> =
        panggil("getHasilPanenSiklus") { remoteDataSource.getHasilPanenSiklus(siklusId) }

    suspend fun getHasilPanenDashboard(): ApiResponse<GetHasilPanenDashboardResponse> =
        panggil("getHasilPanenDashboard") { remoteDataSource.getHasilPanenDashboard() }

    /** Pilihan media telur, cuaca, dan musim dari GET /api/prediksi/opsi. */
    suspend fun getOpsi(): ApiResponse<Opsi> = try {
        val opsi = remoteDataSource.getOpsi().data?.opsi
        if (opsi?.mediaTelur.isNullOrEmpty()) {
            ApiResponse.Error(com.codelabs.gardahijau.model.remote.network.PesanGalat.dariHttp(503, null))
        } else {
            ApiResponse.Success(opsi!!)
        }
    } catch (e: Exception) {
        Log.e(TAG, "getOpsi gagal", e)
        ApiResponse.Error(e.pesanGalat())
    }

    private inline fun <T> panggil(nama: String, blok: () -> T): ApiResponse<T> = try {
        ApiResponse.Success(blok())
    } catch (e: Exception) {
        Log.e(TAG, "$nama gagal", e)
        ApiResponse.Error(e.pesanGalat())
    }

    companion object {
        private const val TAG = "PrediksiRepository"
        const val JENIS_PENETASAN = "PENETASAN"
        const val JENIS_PEMBESARAN = "PEMBESARAN"
        const val JENIS_PANEN = "PANEN"
    }
}
