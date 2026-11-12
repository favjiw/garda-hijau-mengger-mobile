package com.codelabs.gardahijau.model.remote.network

import com.codelabs.gardahijau.model.remote.body.AddSiklusRequest
import com.codelabs.gardahijau.model.remote.body.FaseRequest
import com.codelabs.gardahijau.model.remote.body.LoginRequest
import com.codelabs.gardahijau.model.remote.body.RegisterRequest
import com.codelabs.gardahijau.model.remote.body.TanyaRequest
import com.codelabs.gardahijau.model.remote.body.addHistoryRequest
import com.codelabs.gardahijau.model.remote.response.auth.LogoutResponse
import com.codelabs.gardahijau.model.remote.response.auth.MeResponse
import com.codelabs.gardahijau.model.remote.response.auth.login.LoginResponse
import com.codelabs.gardahijau.model.remote.response.auth.register.RegisterResponse
import com.codelabs.gardahijau.model.remote.response.chatbot.ChatbotResponse
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.AddHistoryPencacahanResponse
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.GetHistoryPencacahanResponse
import com.codelabs.gardahijau.model.remote.response.notification.MarkAsReadResponse
import com.codelabs.gardahijau.model.remote.response.notification.NotificationResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFasePanenResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenDashboardResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.OpsiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Endpoint API Garda Hijau (dokumentasi: <BASE_URL>/api-docs).
 * Semua endpoint selain register & login butuh header Authorization (dipasang AuthInterceptor).
 */
interface MainApiService {

    // ---------- Akun

    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    /** Cek token masih berlaku. 401 -> AuthInterceptor mengarahkan ke halaman Masuk. */
    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @POST("api/auth/logout")
    suspend fun logout(): LogoutResponse

    // ---------- Siklus & fase

    @POST("api/siklus")
    suspend fun addSiklus(@Body siklusRequest: AddSiklusRequest): AddSiklusResponse

    @GET("api/siklus")
    suspend fun getSiklus(): GetSiklusResponse

    /** PENETASAN / PEMBESARAN: `data` = fase. */
    @POST("api/siklus/{siklusId}/fase")
    suspend fun addFase(
        @Path("siklusId") siklusId: Int,
        @Body faseRequest: FaseRequest
    ): AddFaseResponse

    /** PANEN: memicu prediksi AI (bisa ±15 detik). `data` = { fase, prediksiPanen }. */
    @POST("api/siklus/{siklusId}/fase")
    suspend fun addFasePanen(
        @Path("siklusId") siklusId: Int,
        @Body faseRequest: FaseRequest
    ): AddFasePanenResponse

    @GET("api/siklus/{siklusId}/fase")
    suspend fun getFase(@Path("siklusId") siklusId: Int): GetFaseResponse

    // ---------- Pencacahan sampah

    @POST("api/pencacahan")
    suspend fun addHistoryPencacahan(@Body historyRequest: addHistoryRequest): AddHistoryPencacahanResponse

    @GET("api/pencacahan")
    suspend fun getHistoryPencacahan(): GetHistoryPencacahanResponse

    // ---------- Prediksi

    @GET("api/prediksi/siklus/{siklusId}")
    suspend fun getHasilPanenSiklus(@Path("siklusId") siklusId: Int): GetHasilPanenSiklusResponse

    @GET("api/prediksi/total")
    suspend fun getHasilPanenDashboard(): GetHasilPanenDashboardResponse

    /** Pilihan media telur, cuaca, musim dari model AI. Jangan di-hardcode. */
    @GET("api/prediksi/opsi")
    suspend fun getOpsi(): OpsiResponse

    // ---------- Notifikasi

    @GET("api/notifikasi")
    suspend fun getNotifikasi(): NotificationResponse

    @PATCH("api/notifikasi/{id}/read")
    suspend fun markAsRead(@Path("id") id: Int): MarkAsReadResponse

    // ---------- Chatbot

    /** Tanya sekali. Tampilkan hanya data.jawaban. Batas 20 pertanyaan/menit (429). */
    @POST("api/chatbot/tanya")
    suspend fun tanyaChatbot(@Body body: TanyaRequest): ChatbotResponse
}
