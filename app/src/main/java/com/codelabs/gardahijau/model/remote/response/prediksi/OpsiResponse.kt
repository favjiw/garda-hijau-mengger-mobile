package com.codelabs.gardahijau.model.remote.response.prediksi

/** Balasan GET /api/prediksi/opsi: pilihan dropdown dari model AI. Jangan di-hardcode di aplikasi. */
data class OpsiResponse(
    val success: Boolean? = null,
    val data: OpsiData? = null,
)

data class OpsiData(
    val opsi: Opsi? = null,
)

data class Opsi(
    val mediaTelur: List<String>? = null,
    val cuaca: List<String>? = null,
    val musim: List<String>? = null,
)
