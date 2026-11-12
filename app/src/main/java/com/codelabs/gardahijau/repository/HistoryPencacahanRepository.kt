package com.codelabs.gardahijau.repository

import com.codelabs.gardahijau.model.remote.body.addHistoryRequest
import com.codelabs.gardahijau.model.remote.response.RemoteDataSource
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.AddHistoryPencacahanResponse
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.GetHistoryPencacahanResponse
import javax.inject.Inject

class HistoryPencacahanRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun addHistoryPencacahan(
        tanggalWaktu: String,
        totalSampah: Double,
        catatan: String
    ): AddHistoryPencacahanResponse {
        return remoteDataSource.addHistoryPencacahan(tanggalWaktu, totalSampah, catatan)
    }

    suspend fun getHistoryPencacahan(): GetHistoryPencacahanResponse {
        return remoteDataSource.getHistoryPencacahan()
    }
}
