package com.codelabs.gardahijau.model.remote.response

import android.util.Log
import com.codelabs.gardahijau.model.remote.body.AddFasePembesaranRequest
import com.codelabs.gardahijau.model.remote.body.AddFaseRequest
import com.codelabs.gardahijau.model.remote.body.AddSiklusRequest
import com.codelabs.gardahijau.model.remote.body.ChatBody
import com.codelabs.gardahijau.model.remote.body.LoginRequest
import com.codelabs.gardahijau.model.remote.body.RegisterRequest
import com.codelabs.gardahijau.model.remote.body.addHistoryRequest
import com.codelabs.gardahijau.model.remote.network.ChatbotApiService
import com.codelabs.gardahijau.model.remote.network.MainApiService
import com.codelabs.gardahijau.model.remote.response.auth.login.LoginResponse
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.AddHistoryPencacahanResponse
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.GetHistoryPencacahanResponse
import com.codelabs.gardahijau.model.remote.response.notification.NotificationResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenDashboardResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetSiklusResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val mainApiService: MainApiService,
) {
    suspend fun loginAccount(username: String, password: String): LoginResponse =
        mainApiService.login(LoginRequest(username, password))

    suspend fun registerAccount(
        username: String,
        password: String,
        rw: String
    ) = mainApiService.register(
        RegisterRequest(
            username = username,
            rw = rw,
            password = password
        )
    )

    suspend fun getSiklus(): GetSiklusResponse = mainApiService.getSiklus()

    suspend fun addSiklus(
        tanggalMulai: String,
        jumlahTelur: Int,
        mediaTelur: String,
        catatan: String
    ): AddSiklusResponse {
        val request = AddSiklusRequest(tanggalMulai, jumlahTelur, mediaTelur, catatan)
        return mainApiService.addSiklus(request)
    }

    suspend fun addFase(
        siklusId: Int,
        faseData: AddFaseRequest
    ): AddFaseResponse {
        return mainApiService.addFase(siklusId, faseData)
    }

    suspend fun addFasePembesaran(
        siklusId: Int,
        faseData: AddFasePembesaranRequest
    ): AddFaseResponse {
        return mainApiService.addFasePembesaran(siklusId, faseData)
    }

    suspend fun addHistoryPencacahan(
        tanggalWaktu: String,
        totalSampah: Double,
        catatan: String
    ): AddHistoryPencacahanResponse {
        val request = addHistoryRequest(tanggalWaktu, totalSampah, catatan)
        return mainApiService.addHistoryPencacahan(request)
    }

    suspend fun getHistoryPencacahan(): GetHistoryPencacahanResponse {
        return mainApiService.getHistoryPencacahan()
    }

    suspend fun getNotification(): NotificationResponse {
        return mainApiService.getNotifikasi()
    }

    suspend fun markAsRead(id: Int): Boolean {
        return mainApiService.markAsRead(id).updated == 1
    }


    suspend fun addFasePanen(
        siklusId: Int,
        faseData: AddFasePembesaranRequest
    ): AddFaseResponse {
        return mainApiService.addFasePembesaran(siklusId, faseData)
    }

    suspend fun getFase(siklusId: Int): GetFaseResponse = mainApiService.getFase(siklusId)

    suspend fun getHasilPanenSiklus(siklusId: Int): GetHasilPanenSiklusResponse =
        mainApiService.getHasilPanenSiklus(siklusId)

    suspend fun getHasilPanenDashboard(): GetHasilPanenDashboardResponse =
        mainApiService.getHasilPanenDashboard()
}