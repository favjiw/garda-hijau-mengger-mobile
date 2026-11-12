package com.codelabs.gardahijau.model.remote.response

import com.codelabs.gardahijau.model.remote.body.AddSiklusRequest
import com.codelabs.gardahijau.model.remote.body.FaseRequest
import com.codelabs.gardahijau.model.remote.body.LoginRequest
import com.codelabs.gardahijau.model.remote.body.RegisterRequest
import com.codelabs.gardahijau.model.remote.body.addHistoryRequest
import com.codelabs.gardahijau.model.remote.network.MainApiService
import com.codelabs.gardahijau.model.remote.response.auth.login.LoginResponse
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.AddHistoryPencacahanResponse
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.GetHistoryPencacahanResponse
import com.codelabs.gardahijau.model.remote.response.notification.NotificationResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFasePanenResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenDashboardResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.OpsiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val mainApiService: MainApiService,
) {
    suspend fun loginAccount(username: String, password: String): LoginResponse =
        mainApiService.login(LoginRequest(username, password))

    suspend fun registerAccount(username: String, password: String, rw: String) =
        mainApiService.register(RegisterRequest(username = username, rw = rw, password = password))

    suspend fun getSiklus(): GetSiklusResponse = mainApiService.getSiklus()

    suspend fun addSiklus(
        tanggalMulai: String,
        jumlahTelur: Int,
        mediaTelur: String,
        catatan: String
    ): AddSiklusResponse =
        mainApiService.addSiklus(AddSiklusRequest(tanggalMulai, jumlahTelur, mediaTelur, catatan))

    suspend fun addFase(siklusId: Int, fase: FaseRequest): AddFaseResponse =
        mainApiService.addFase(siklusId, fase)

    suspend fun addFasePanen(siklusId: Int, fase: FaseRequest): AddFasePanenResponse =
        mainApiService.addFasePanen(siklusId, fase)

    suspend fun getFase(siklusId: Int): GetFaseResponse = mainApiService.getFase(siklusId)

    suspend fun addHistoryPencacahan(
        tanggalWaktu: String,
        totalSampah: Double,
        catatan: String
    ): AddHistoryPencacahanResponse =
        mainApiService.addHistoryPencacahan(addHistoryRequest(tanggalWaktu, totalSampah, catatan))

    suspend fun getHistoryPencacahan(): GetHistoryPencacahanResponse = mainApiService.getHistoryPencacahan()

    suspend fun getNotification(): NotificationResponse = mainApiService.getNotifikasi()

    suspend fun markAsRead(id: Int): Boolean = mainApiService.markAsRead(id).updated == 1

    suspend fun getHasilPanenSiklus(siklusId: Int): GetHasilPanenSiklusResponse =
        mainApiService.getHasilPanenSiklus(siklusId)

    suspend fun getHasilPanenDashboard(): GetHasilPanenDashboardResponse =
        mainApiService.getHasilPanenDashboard()

    suspend fun getOpsi(): OpsiResponse = mainApiService.getOpsi()
}
