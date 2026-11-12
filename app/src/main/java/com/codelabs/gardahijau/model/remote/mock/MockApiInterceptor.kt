package com.codelabs.gardahijau.model.remote.mock

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * MODE DEMO — hanya aktif bila BuildConfig.USE_MOCK_API = true (build debug).
 *
 * Menjawab semua request ke API utama dan chatbot dengan data contoh dari [MockApiStore],
 * tanpa menyentuh server. Dipakai untuk mengecek tampilan saat server mati.
 */
class MockApiInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val bodyText = request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }.orEmpty()

        val (code, json) = MockApiStore.handle(request.method, request.url.encodedPath, bodyText)

        // Jeda kecil supaya indikator memuat tetap terlihat seperti memakai server sungguhan.
        Thread.sleep(MOCK_DELAY_MS)

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(json.toResponseBody(JSON))
            .build()
    }

    private companion object {
        const val MOCK_DELAY_MS = 350L
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
