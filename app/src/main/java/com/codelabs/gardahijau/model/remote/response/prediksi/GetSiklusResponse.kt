package com.codelabs.gardahijau.model.remote.response.prediksi

/** Balasan GET /api/siklus. Field dibuat boleh null karena contoh balasan belum ada di dokumentasi API. */
data class GetSiklusResponse(
	val data: List<DataItem>,
	val success: Boolean
)

data class DataItem(
	val createdAt: String? = null,
	val tanggalMulai: String? = null,
	val catatan: String? = null,
	val mediaTelur: String? = null,
	val id: Int = 0,
	val userId: Int? = null,
	val jumlahTelur: Int? = null,
	val prediksiPanen: Any? = null,
)
