package com.codelabs.gardahijau

import com.codelabs.gardahijau.model.remote.body.FaseRequest
import com.codelabs.gardahijau.model.remote.body.TanyaRequest
import com.codelabs.gardahijau.model.remote.network.PesanGalat
import com.codelabs.gardahijau.model.remote.response.auth.MeResponse
import com.codelabs.gardahijau.model.remote.response.auth.login.LoginResponse
import com.codelabs.gardahijau.model.remote.response.chatbot.ChatbotResponse
import com.codelabs.gardahijau.model.remote.response.notification.NotificationResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFasePanenResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.AddFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetFaseResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenDashboardResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.GetHasilPanenSiklusResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.OpsiResponse
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Memastikan model aplikasi bisa membaca contoh JSON dari dokumentasi API
 * (https://gardahijau.codelabspace.or.id/api-docs) dan pesan galat sesuai status code.
 */
class ApiKontrakTest {

    private val gson = Gson()

    @Test
    fun login_bacaToken() {
        val json = """{"success":true,"message":"Login berhasil","token":"eyJhbGciOiJIUzI1NiIs","expiresIn":"7d",
            "user":{"id":1,"username":"rw4","rw":"RW4"}}"""
        val res = gson.fromJson(json, LoginResponse::class.java)
        assertEquals("eyJhbGciOiJIUzI1NiIs", res.token)
        assertEquals("RW4", res.user.rw)
    }

    @Test
    fun me_bacaUser() {
        val json = """{"success":true,"user":{"id":1,"username":"rw4","rw":"RW4","createdAt":"2025-11-14T10:00:00.000Z"}}"""
        val res = gson.fromJson(json, MeResponse::class.java)
        assertEquals("rw4", res.user?.username)
    }

    @Test
    fun fasePanen_bacaPrediksiDenganDesimal() {
        val json = """{"success":true,"data":{"fase":{"id":12,"jenis":"PANEN","tanggal":"2025-11-20T00:00:00.000Z",
            "jumlahTelur":40,"jumlahMakanan":20000,"keterangan":"Siap panen","createdAt":"2025-11-20T08:00:00.000Z","siklusId":3},
            "prediksiPanen":{"id":7,"hasilGram":3768.01,"hasilKg":3.768,"conversionRate":18.84,"conversionLabel":"Cukup",
            "roiEstimate":41.3,"estimatedValue":56520.5,"feedCost":40000,"rekomendasi":["Tambah pakan."],
            "createdAt":"2025-11-20T08:00:00.000Z","faseId":12}}}"""
        val res = gson.fromJson(json, AddFasePanenResponse::class.java)
        assertTrue(res.success)
        assertEquals(3.768, res.data?.prediksiPanen?.hasilKg!!, 0.0001)
        assertEquals(56520.5, res.data?.prediksiPanen?.estimatedValue!!, 0.0001)
        assertEquals(20000.0, res.data?.fase?.jumlahMakanan!!, 0.0001)
        assertEquals(listOf("Tambah pakan."), res.data?.prediksiPanen?.rekomendasi)
    }

    @Test
    fun fasePembesaran_bolehNull() {
        val json = """{"success":true,"data":{"id":11,"jenis":"PEMBESARAN","tanggal":"2025-11-15T00:00:00.000Z",
            "jumlahTelur":null,"jumlahMakanan":null,"keterangan":"Mulai pembesaran","createdAt":"2025-11-15T08:00:00.000Z","siklusId":3}}"""
        val res = gson.fromJson(json, AddFaseResponse::class.java)
        assertTrue(res.success)
        assertNull(res.data?.jumlahMakanan)
        assertEquals("PEMBESARAN", res.data?.jenis)
    }

    @Test
    fun daftarFase_bacaPrediksiSebagaiMap() {
        val json = """{"success":true,"data":[
            {"id":10,"jenis":"PENETASAN","tanggal":"2025-11-13T00:00:00.000Z","jumlahTelur":40,"jumlahMakanan":null,
             "keterangan":null,"createdAt":"2025-11-13T08:00:00.000Z","siklusId":3,"prediksiPanen":null},
            {"id":11,"jenis":"PEMBESARAN","tanggal":"2025-11-15T00:00:00.000Z","jumlahTelur":null,"jumlahMakanan":20000.5,
             "keterangan":"Mulai","createdAt":"2025-11-15T08:00:00.000Z","siklusId":3,"prediksiPanen":null},
            {"id":12,"jenis":"PANEN","tanggal":"2025-11-20T00:00:00.000Z","jumlahTelur":40,"jumlahMakanan":20000,
             "keterangan":"Siap panen","createdAt":"2025-11-20T08:00:00.000Z","siklusId":3,
             "prediksiPanen":{"id":7,"hasilGram":3768.01,"hasilKg":3.768}}]}"""
        val res = gson.fromJson(json, GetFaseResponse::class.java)
        assertEquals(3, res.data.size)
        assertEquals(20000.5, res.data[1].jumlahMakanan!!, 0.0001)
        val prediksi = res.data[2].prediksiPanen as Map<*, *>
        assertEquals(3.768, prediksi["hasilKg"] as Double, 0.0001)
    }

    @Test
    fun prediksiTotal_dan_perSiklus() {
        val total = gson.fromJson("""{"success":true,"totalKg":12.54,"totalGram":12540,"jumlahPrediksi":4}""",
            GetHasilPanenDashboardResponse::class.java)
        assertEquals(12540, total.totalGram)
        assertEquals(4, total.jumlahPrediksi)

        val perSiklus = gson.fromJson("""{"success":true,"data":[{"id":7,"hasilGram":3768.01,"hasilKg":3.768,
            "estimatedValue":56520.75,"feedCost":40000.5,"conversionLabel":"Cukup","faseId":12}]}""",
            GetHasilPanenSiklusResponse::class.java)
        assertEquals(56520.75, perSiklus.data[0].estimatedValue!!, 0.0001)
    }

    @Test
    fun opsi_bacaMediaTelur() {
        val json = """{"success":true,"data":{"opsi":{"mediaTelur":["Ampas atau Limbah Organik Basah",
            "Campuran Media (Kombinasi Kering & Basah)","Dedak atau Bekatul","Kotoran Ternak (Fermentasi)"],
            "cuaca":["Clear","Clouds"],"musim":["Hujan","Kemarau","Pancaroba"]},
            "model":{"penetasan":{"nama":"Gradient Boosting Classifier","akurasi":"78.00%"}}}}"""
        val res = gson.fromJson(json, OpsiResponse::class.java)
        assertEquals(4, res.data?.opsi?.mediaTelur?.size)
        assertEquals("Dedak atau Bekatul", res.data?.opsi?.mediaTelur?.get(2))
    }

    @Test
    fun chatbot_hanyaJawaban() {
        val json = """{"success":true,"data":{"jawaban":"Suhu ideal 27-30°C.","sumber":[{"dokumen":"a.pdf","cuplikan":"...","kemiripan":0.82}]}}"""
        val res = gson.fromJson(json, ChatbotResponse::class.java)
        assertEquals("Suhu ideal 27-30°C.", res.data?.jawaban)
    }

    @Test
    fun notifikasi_bacaDaftar() {
        val json = """{"success":true,"data":[{"id":1,"userId":1,"title":"Siklus Baru Dibuat","message":"Siklus baru dimulai dengan 40 telur.",
            "type":"success","isRead":false,"createdAt":"2025-11-14T10:00:00.000Z"}]}"""
        val res = gson.fromJson(json, NotificationResponse::class.java)
        assertEquals(1, res.data.size)
        assertFalse(res.data[0].isRead)
    }

    @Test
    fun bodyRequest_sesuaiDokumentasi() {
        val panen = JsonParser.parseString(
            gson.toJson(FaseRequest(jenis = "PANEN", tanggal = "2025-11-20", jumlahTelur = 40, jumlahMakanan = 20000.0, keterangan = "Siap panen"))
        ).asJsonObject
        assertEquals(setOf("jenis", "tanggal", "jumlahTelur", "jumlahMakanan", "keterangan"), panen.keySet())

        // Field null tidak ikut dikirim.
        val pembesaran = JsonParser.parseString(gson.toJson(FaseRequest(jenis = "PEMBESARAN", tanggal = "2025-11-15"))).asJsonObject
        assertEquals(setOf("jenis", "tanggal"), pembesaran.keySet())

        val tanya = JsonParser.parseString(gson.toJson(TanyaRequest("Halo"))).asJsonObject
        assertEquals("Halo", tanya.get("pertanyaan").asString)
    }

    // ------------------------------------------------------------------ pesan galat

    private fun httpError(code: Int, body: String): HttpException =
        HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))

    @Test
    fun galat400_pakaiPesanServer() {
        val e = httpError(400, """{"success":false,"message":"Username atau password salah"}""")
        assertEquals("Username atau password salah", PesanGalat.dari(e))
    }

    @Test
    fun galat401_selaluMintaMasukLagi() {
        val e = httpError(401, """{"success":false,"message":"Token tidak valid"}""")
        assertEquals("Sesi kamu sudah berakhir. Silakan masuk lagi.", PesanGalat.dari(e))
    }

    @Test
    fun galat429_dan503_tanpaPesan_pakaiBawaan() {
        assertEquals("Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi.", PesanGalat.dari(httpError(429, "")))
        assertEquals("Layanan AI sedang tidak tersedia. Coba lagi nanti.", PesanGalat.dari(httpError(503, "<html>Bad gateway</html>")))
    }

    @Test
    fun galat503_denganPesanServer() {
        val e = httpError(503, """{"success":false,"message":"Layanan prediksi sedang sibuk"}""")
        assertEquals("Layanan prediksi sedang sibuk", PesanGalat.dari(e))
    }

    @Test
    fun galatJaringan_dan_waktuHabis() {
        assertEquals("Tidak bisa terhubung ke server. Periksa internet lalu coba lagi.", PesanGalat.dari(IOException("no route")))
        assertEquals("Server terlalu lama menjawab. Coba lagi.", PesanGalat.dari(SocketTimeoutException("timeout")))
    }
}
