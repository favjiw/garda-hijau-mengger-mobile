package com.codelabs.gardahijau.model.remote.response.prediksi

data class AddSiklusResponse(
	val data: DataFase,
	val success: Boolean
)

data class DataFase(
	val createdAt: String,
	val tanggalMulai: String,
	val catatan: String,
	val mediaTelur: String,
	val id: Int,
	val userId: Int,
	val jumlahTelur: Int
)

