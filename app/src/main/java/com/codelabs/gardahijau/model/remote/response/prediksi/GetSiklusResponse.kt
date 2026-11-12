package com.codelabs.gardahijau.model.remote.response.prediksi

data class GetSiklusResponse(
	val data: List<DataItem>,
	val success: Boolean
)

data class DataItem(
	val createdAt: String,
	val tanggalMulai: String,
	val catatan: String,
	val mediaTelur: String,
	val id: Int,
	val userId: Int,
	val jumlahTelur: Int,
	val prediksiPanen: Any,
)

