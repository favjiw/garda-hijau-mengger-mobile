package com.codelabs.gardahijau.repository

import android.util.Log
import com.codelabs.gardahijau.model.remote.body.AddFasePembesaranRequest
import com.codelabs.gardahijau.model.remote.body.AddFaseRequest
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.RemoteDataSource
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenDashboardResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetSiklusResponse
import javax.inject.Inject

class PrediksiRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) {
    suspend fun getSiklus(): ApiResponse<GetSiklusResponse> {
        return try {
            val response = remoteDataSource.getSiklus()
            Log.d("PrediksiRepository", "API Response: $response")
            Log.d("PrediksiRepository", "Success: ${response.success}, Data size: ${response.data?.size}")

            if (response.success) {
                ApiResponse.Success(response)
            } else {
                ApiResponse.Error("Failed to fetch siklus data")
            }
        } catch (e: Exception) {
            Log.e("PrediksiRepository", "Error: ${e.message}", e)
            ApiResponse.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun addSiklus(
        tanggalMulai: String,
        jumlahTelur: Int,
        mediaTelur: String,
        catatan: String
    ): ApiResponse<AddSiklusResponse> {
        return try {
            val siklusResponse = remoteDataSource.addSiklus(tanggalMulai, jumlahTelur, mediaTelur, catatan)
            Log.d("PrediksiRepository", "Add Siklus Response: $siklusResponse")

            if (siklusResponse.success) {
                val siklusId = siklusResponse.data.id
                try {
                    val faseRequest = AddFaseRequest(
                        tanggal= tanggalMulai,
                        jenis = "PENETASAN",
                        jumlahTelur = jumlahTelur,
                        mediaTelur = mediaTelur,
                        catatan = "Fase awal setelah penetasan"
                    )
                    val faseResponse = remoteDataSource.addFase(siklusId, faseRequest)
                    Log.d("PrediksiRepository", "Add Fase Response: $faseResponse")

                    if (!faseResponse.success) {
                        Log.w("PrediksiRepository", "Siklus created but fase creation failed")
                    }
                } catch (faseException: Exception) {
                    Log.e("PrediksiRepository", "Error adding fase: ${faseException.message}", faseException)
                }

                ApiResponse.Success(siklusResponse)
            } else {
                ApiResponse.Error("Failed to add siklus")
            }
        } catch (e: Exception) {
            Log.e("PrediksiRepository", "Error adding siklus: ${e.message}", e)
            ApiResponse.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getFase(siklusId: Int): ApiResponse<GetFaseResponse> {
        return try {
            val response = remoteDataSource.getFase(siklusId)
            Log.d("PrediksiRepository", "Get Fase Response: $response")

            if (response.success) {
                ApiResponse.Success(response)
            } else {
                ApiResponse.Error("Failed to fetch fase data")
            }
        } catch (e: Exception) {
            Log.e("PrediksiRepository", "Error getting fase: ${e.message}", e)
            ApiResponse.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun addFase(
        siklusId: Int,
        jenis: String,
        tanggal: String,
        jumlahTelur: Int,
        mediaTelur: String,
        catatan: String
    ): ApiResponse<AddFaseResponse> {
        return try {
            val faseRequest = AddFaseRequest(
                jenis = jenis,
                tanggal = tanggal,
                jumlahTelur = jumlahTelur,
                mediaTelur = mediaTelur,
                catatan = catatan
            )

            val response = remoteDataSource.addFase(siklusId, faseRequest)
            Log.d("PrediksiRepository", "Add Fase Response: $response")

            if (response.success) {
                ApiResponse.Success(response)
            } else {
                ApiResponse.Error("Gagal menambahkan fase")
            }
        } catch (e: Exception) {
            Log.e("PrediksiRepository", "Error adding fase: ${e.message}", e)
            ApiResponse.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun addFasePembesaran(
        siklusId: Int,
        jenis: String,
        tanggal: String,
        jumlahMakanan: Int,
        catatan: String
    ): ApiResponse<AddFaseResponse> {
        return try {
            val faseRequest = AddFasePembesaranRequest(
                jenis = jenis,
                tanggal = tanggal,
                jumlahMakanan = jumlahMakanan,
                catatan = catatan
            )

            val response = remoteDataSource.addFasePembesaran(siklusId, faseRequest)
            Log.d("PrediksiRepository", "Add Fase Response: $response")

            if (response.success) {
                ApiResponse.Success(response)
            } else {
                ApiResponse.Error("Gagal menambahkan fase")
            }
        } catch (e: Exception) {
            Log.e("PrediksiRepository", "Error adding fase: ${e.message}", e)
            ApiResponse.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun addFasePanen(
        siklusId: Int,
        jenis: String,
        tanggal: String,
        jumlahHasil: Int, // hasil panen
        catatan: String
    ): ApiResponse<AddFaseResponse> {
        return try {
            val faseRequest = AddFaseRequest(
                jenis = jenis,
                tanggal = tanggal,
                jumlahTelur = jumlahHasil,
                mediaTelur = "",
                catatan = catatan
            )

            val response = remoteDataSource.addFase(siklusId, faseRequest)
            Log.d("PrediksiRepository", "Add Fase Panen Response: $response")

            if (response.success) {
                ApiResponse.Success(response)
            } else {
                ApiResponse.Error("Gagal menambahkan fase panen")
            }
        } catch (e: Exception) {
            Log.e("PrediksiRepository", "Error adding fase panen: ${e.message}", e)
            ApiResponse.Error(e.message ?: "Terjadi kesalahan")
        }
    }


    suspend fun getHasilPanenSiklus(siklusId: Int): ApiResponse<GetHasilPanenSiklusResponse> {
        return try {
            val response = remoteDataSource.getHasilPanenSiklus(siklusId)
            if (response.success) ApiResponse.Success(response) else ApiResponse.Error("Failed to fetch hasil panen")
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getHasilPanenDashboard(): ApiResponse<GetHasilPanenDashboardResponse> {
        return try {
            val response = remoteDataSource.getHasilPanenDashboard()
            ApiResponse.Success(response)
        } catch (e: Exception) {
            ApiResponse.Error(e.message.toString())
        }
    }


}

