package com.codelabs.gardahijau

import com.codelabs.gardahijau.model.local.antrean.KlasifikasiAntrean
import com.codelabs.gardahijau.model.local.antrean.KlasifikasiAntrean.Aksi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/** Uji keputusan antrean offline: galat mana yang diulang, dibuang, atau menghentikan kiriman. */
class AntreanTest {

    private fun httpError(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "{}".toResponseBody("application/json".toMediaType())))

    @Test
    fun jaringanDanTimeout_diulang() {
        assertEquals(Aksi.ULANGI, KlasifikasiAntrean.aksi(IOException("no route")))
        assertEquals(Aksi.ULANGI, KlasifikasiAntrean.aksi(SocketTimeoutException("timeout")))
    }

    @Test
    fun sesi401_menghentikanKiriman() {
        assertEquals(Aksi.BERHENTI, KlasifikasiAntrean.aksi(httpError(401)))
    }

    @Test
    fun permintaanDitolak400_dibuang() {
        assertEquals(Aksi.BUANG, KlasifikasiAntrean.aksi(httpError(400)))
        assertEquals(Aksi.BUANG, KlasifikasiAntrean.aksi(httpError(404)))
    }

    @Test
    fun serverDanRateLimit_diulang() {
        assertEquals(Aksi.ULANGI, KlasifikasiAntrean.aksi(httpError(429)))
        assertEquals(Aksi.ULANGI, KlasifikasiAntrean.aksi(httpError(500)))
        assertEquals(Aksi.ULANGI, KlasifikasiAntrean.aksi(httpError(503)))
    }
}
